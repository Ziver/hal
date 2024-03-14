import Alert from 'AlertComponent'
import { alertStore } from 'AlertStore'

export default {
  data() {
    return {
      alertStore
    }
  },
  components: {
    Alert,
  },
  mounted() {
    alertStore.enableAutoLoad(true);
  },
  template: `
    <div v-if="alertStore.loading" class="modal fade in" tabindex="-1" role="dialog" aria-hidden="true" data-backdrop="false">
      <div class="modal-dialog modal-sm">
        <div class="modal-content modal-body">
          <i class="glyphicon glyphicon-refresh glyphicon-refresh-animate"></i>
          Loading...
        </div>
      </div>
    </div>

    <div id="alert-container">
      <Alert v-for="a in alertStore.alerts" v-bind="a" />
    </div>`
}