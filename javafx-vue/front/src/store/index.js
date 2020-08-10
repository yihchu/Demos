import Vue from 'vue'
import Vuex from 'vuex';

Vue.use(Vuex)

const TMP_TYPE = 'TMP_TYPE'

const store = new Vuex.Store({
  strict: true,
  modules: {
    moduleA: {
      state: {
        tmp: 'test'
      },
      mutations: {
        [TMP_TYPE] (state, payload) {
          state.tmp = payload
        }
      },
      getters: {
        tmp: state => {
          return state.tmp
        }
      },
      actions: {
        updateTmp ({commit}, payload) {
          commit(TMP_TYPE, payload)
        }
      }
    }
  }
})

export default store;