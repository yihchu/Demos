import Vue from 'vue';

const mixin = {
  methods : {
    // $formValidate(form){
    //   if( form ){
    //       return new Promise( ( resolve ,reject ) => form.validate((valid) => resolve(valid) ));
    //   }else{
    //       return false;
    //   }
    // },
    //
    // $format(date) {
    //   return format(new Date(date), 'YYYY-MM-DD HH:mm:ss');
    // }

  }
}


Vue.mixin(mixin);