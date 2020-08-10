import Vue        from 'vue';
import Axios      from 'axios/dist/axios';

const axios = Axios.create({
    baseURL: `${AXIOS_ENDPOINT}`
});

Vue.prototype.$http = axios;
Vue.prototype.$get  = async function( url , params ){
    return (await axios.get(url , {
        params : params
    })).data;
}

Vue.prototype.$post  = async function( url , params ){
    return (await axios.post(url , params )).data;
}
//
// Vue.prototype.$postForm  = async function( url , params ){
//     let fd = new FormData();
//     for( let [k,v] of Object.entries(params) ){
//         fd.append(k,v);
//     }
//     return (await axios.post(url , fd )).data;
// }