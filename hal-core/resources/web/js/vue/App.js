import AlertList from 'AlertListComponent'

export default {
  data() {
    return {
    }
  },
  components: {
    AlertList,
  },
  template: `
    <div class="container col-md-12">
      <AlertList />

      <router-view></router-view>
    </div>`
}