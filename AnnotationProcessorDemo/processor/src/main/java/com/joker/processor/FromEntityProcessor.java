package com.joker.processor;


import com.google.auto.service.AutoService;
import com.joker.annotation.FromEntity;
import com.joker.util.ProcessUtil;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 参考：
 * https://plugins.jetbrains.com/docs/intellij/syntax-highlighting-and-error-highlighting.html
 * https://blog.mythsman.com/post/5d2c11c767f841464434a3bf/
 * https://liuyehcf.github.io/2018/02/02/Java-JSR-269-%E6%8F%92%E5%85%A5%E5%BC%8F%E6%B3%A8%E8%A7%A3%E5%A4%84%E7%90%86%E5%99%A8/
 */
@SupportedAnnotationTypes({"com.joker.annotation.FromEntity"})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class FromEntityProcessor extends AbstractProcessor {
    private Messager messager;
    private JavacTrees trees;
    private TreeMaker treeMaker;
    private Names names;

    private List<JCTree.JCVariableDecl> fieldJCVariables;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.trees = JavacTrees.instance(processingEnv);
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.treeMaker = TreeMaker.instance(context);
        this.names = Names.instance(context);
    }

    @Override
    public boolean process(Set<? extends TypeElement> elements, RoundEnvironment roundEnv) {

        Map<String, Element> map = roundEnv.getRootElements().stream()
                .collect(Collectors.toMap(ele -> ele.asType().toString(), Function.identity()));

        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(FromEntity.class);
        set.forEach(element -> {
            FromEntity fromEntity = element.getAnnotation(FromEntity.class);
            String mappings = fromEntity.mappings();
            String pkg = fromEntity.clazz();
            if (element.getKind() == ElementKind.CLASS) {
                addImportInfo(element, pkg);
            }
            JCTree jcTree = trees.getTree(element);
            jcTree.accept(new TreeTranslator() {
                @Override
                public void visitClassDef(JCTree.JCClassDecl jcClass) {
                    messager.printMessage(Diagnostic.Kind.NOTE, "process class [" + jcClass.name.toString() + "], start");

                    before(jcClass);

                    if (!ProcessUtil.hasFromEntityConstructor(fieldJCVariables, pkg, jcClass)) {
                        JCTree.JCMethodDecl method = fromEntityConstructor(element, mappings, map.get(pkg));
                        jcClass.defs = jcClass.defs.append(method);
                    }

                    after();

                    super.visitClassDef(jcClass);
                    messager.printMessage(Diagnostic.Kind.NOTE, "process class [" + jcClass.name.toString() + "], end");
                }
            });
        });
        return true;
    }

    private void before(JCTree.JCClassDecl jcClass) {
        this.fieldJCVariables = ProcessUtil.getJCVariables(jcClass);
    }

    private void after() {
        this.fieldJCVariables = null;
    }

    private void addImportInfo(Element element, String classPath) {
        int idx = classPath.lastIndexOf(".");
        String pkg = classPath.substring(0, idx), clz = classPath.substring(idx + 1);
        TreePath treePath = trees.getPath(element);
        Tree leaf = treePath.getLeaf();
        if (treePath.getCompilationUnit() instanceof JCTree.JCCompilationUnit && leaf instanceof JCTree) {
            JCTree.JCCompilationUnit jccu = (JCTree.JCCompilationUnit) treePath.getCompilationUnit();
            for (JCTree jcTree : jccu.getImports()) {
                if (jcTree != null && jcTree instanceof JCTree.JCImport) {
                    JCTree.JCImport jcImport = (JCTree.JCImport) jcTree;
                    if (jcImport.qualid != null && jcImport.qualid instanceof JCTree.JCFieldAccess) {
                        JCTree.JCFieldAccess jcFieldAccess = (JCTree.JCFieldAccess) jcImport.qualid;
                        try {
                            if (pkg.equals(jcFieldAccess.selected.toString()) && clz.equals(jcFieldAccess.name.toString())) {
                                return;
                            }
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            java.util.List<JCTree> trees = new ArrayList<>();
            trees.addAll(jccu.defs);
            JCTree.JCIdent ident = treeMaker.Ident(names.fromString(pkg));
            JCTree.JCImport jcImport = treeMaker.Import(treeMaker.Select(
                    ident, names.fromString(clz)), false);
            idx = 0;
            for (JCTree tree: trees) {
                if (tree instanceof JCTree.JCPackageDecl) {
                    ++idx;
                }
            }
            if (!trees.contains(jcImport)) {
                trees.add(idx, jcImport);
            }
            jccu.defs = List.from(trees);
        }
    }

    private JCTree.JCMethodDecl fromEntityConstructor(Element element, String mappings, Element param) {
        ListBuffer<JCTree.JCVariableDecl> jcVariables = new ListBuffer<>();
        jcVariables.append(
                treeMaker.Param(names.fromString("entity"), (Type) param.asType(), null));
        ListBuffer<JCTree.JCStatement> jcStatements = new ListBuffer<>();
        for (JCTree.JCVariableDecl jcVariable: this.fieldJCVariables) {
            jcStatements.append(
                    treeMaker.Exec(
                            treeMaker.Assign(
                                    treeMaker.Select(
                                            treeMaker.Ident(names.fromString(ProcessUtil.THIS)),
                                            jcVariable.name
                                    ),
                                    treeMaker.Select(
                                            treeMaker.Ident(names.fromString("entity")),
                                            jcVariable.name
                                    )
                            )
                    )
            );
        }
        return treeMaker.MethodDef(
                treeMaker.Modifiers(Flags.PUBLIC),
                names.fromString(ProcessUtil.CONSTRUCTOR_NAME),
                treeMaker.TypeIdent(TypeTag.VOID),
                List.nil(),
                jcVariables.toList(),
                List.nil(),
                treeMaker.Block(0, jcStatements.toList()),
                null
        );
    }
}
