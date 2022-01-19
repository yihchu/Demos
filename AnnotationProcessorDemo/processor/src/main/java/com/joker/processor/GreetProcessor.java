package com.joker.processor;

import com.google.auto.service.AutoService;
import com.joker.annotation.Greet;
import com.joker.util.ProcessUtil;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

import static com.joker.util.ProcessUtil.CONSTRUCTOR_NAME;

/**
 * 参考：
 * https://plugins.jetbrains.com/docs/intellij/syntax-highlighting-and-error-highlighting.html
 * https://blog.mythsman.com/post/5d2c11c767f841464434a3bf/
 * https://liuyehcf.github.io/2018/02/02/Java-JSR-269-%E6%8F%92%E5%85%A5%E5%BC%8F%E6%B3%A8%E8%A7%A3%E5%A4%84%E7%90%86%E5%99%A8/
 */
@SupportedAnnotationTypes({"com.joker.annotation.Greet"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class GreetProcessor extends AbstractProcessor {

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
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(Greet.class);
        set.forEach(element -> {

            JCTree jcTree = trees.getTree(element);
            jcTree.accept(new TreeTranslator() {
                @Override
                public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
                    List<JCTree.JCVariableDecl> jcVariableDeclList = List.nil();

                    for (JCTree tree : jcClassDecl.defs) {
                        if (tree.getKind().equals(Tree.Kind.VARIABLE)) {
                            JCTree.JCVariableDecl jcVariableDecl = (JCTree.JCVariableDecl) tree;
                            jcVariableDeclList = jcVariableDeclList.append(jcVariableDecl);
                        }
                    }
                    jcVariableDeclList.forEach(jcVariableDecl -> {
                        messager.printMessage(Diagnostic.Kind.NOTE, jcVariableDecl.getName() + " has been processed");
                        jcClassDecl.defs = jcClassDecl.defs.prepend(makeGetterMethodDecl(jcVariableDecl));
                    });
                    before(jcClassDecl);
                    JCTree.JCMethodDecl method1 = createNoArgsConstructor();
                    jcClassDecl.defs = jcClassDecl.defs.append(method1);
                    JCTree.JCMethodDecl method2 = createAllArgsConstructor();
                    jcClassDecl.defs = jcClassDecl.defs.append(method2);
                    after();
                    super.visitClassDef(jcClassDecl);
                }
            });
        });
        return true;
    }



    private JCTree.JCMethodDecl makeGetterMethodDecl(JCTree.JCVariableDecl jcVariableDecl) {
        ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();
        statements.append(treeMaker.Return(treeMaker.Select(treeMaker.Ident(names.fromString("this")), jcVariableDecl.getName())));
        JCTree.JCBlock body = treeMaker.Block(0, statements.toList());
        return treeMaker.MethodDef(treeMaker.Modifiers(Flags.PUBLIC), getNewMethodName(jcVariableDecl.getName()), jcVariableDecl.vartype, List.nil(), List.nil(), List.nil(), body, null);
    }

    private Name getNewMethodName(Name name) {
        String s = name.toString();
        return names.fromString("hello" + s.substring(0, 1).toUpperCase() + s.substring(1, name.length()));
    }

    private void before(JCTree.JCClassDecl jcClass) {
        this.fieldJCVariables = ProcessUtil.getJCVariables(jcClass);
    }

    private void after() {
        this.fieldJCVariables = null;
    }

    private JCTree.JCMethodDecl createAllArgsConstructor() {

        ListBuffer<JCTree.JCStatement> jcStatements = new ListBuffer<>();
        for (JCTree.JCVariableDecl jcVariable : fieldJCVariables) {
            //添加构造方法的赋值语句 " this.xxx = xxx; "
            jcStatements.append(
                    treeMaker.Exec(
                            treeMaker.Assign(
                                    treeMaker.Select(
                                            treeMaker.Ident(names.fromString("this")),
                                            names.fromString(jcVariable.name.toString())
                                    ),
                                    treeMaker.Ident(names.fromString(jcVariable.name.toString()))
                            )
                    )
            );
        }
        JCTree.JCBlock jcBlock = treeMaker.Block(
                0 //访问标志
                , jcStatements.toList() //所有的语句
        );
        return treeMaker.MethodDef(
                treeMaker.Modifiers(Flags.PUBLIC), //访问标志
                names.fromString(CONSTRUCTOR_NAME), //名字
                treeMaker.TypeIdent(TypeTag.VOID), //返回类型
                List.nil(), //泛型形参列表
                ProcessUtil.cloneJCVariablesAsParams(treeMaker, fieldJCVariables), //参数列表
                List.nil(), //异常列表
                jcBlock, //方法体
                null //默认方法（可能是interface中的那个default）
        );
    }

    private JCTree.JCMethodDecl createNoArgsConstructor() {
        JCTree.JCBlock jcBlock = treeMaker.Block(
                0 //访问标志
                , List.nil() //所有的语句
        );
        return treeMaker.MethodDef(
                treeMaker.Modifiers(Flags.PUBLIC), //访问标志
                names.fromString("<init>"), //名字
                treeMaker.TypeIdent(TypeTag.VOID), //返回类型
                List.nil(), //泛型形参列表
                List.nil(), //参数列表
                List.nil(), //异常列表
                jcBlock, //方法体
                null //默认方法（可能是interface中的那个default）
        );
    }
}
