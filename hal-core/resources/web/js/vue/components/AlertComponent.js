export default {
  props: [
    "level", // ERROR, WARNING, SUCCESS, INFO
    "message"
  ],
  data() {
    return { }
  },
  computed: {
    levelClass() {
      switch(this.level) {
        case "ERROR":   return "alert-danger";
        case "WARNING": return "alert-warning";
        case "SUCCESS": return "alert-success";
        case "INFO":
        default:        return "alert-info";
      }
    },
    levelIcon() {
      switch(this.level) {
        case "ERROR":   return "glyphicon-minus-sign";
        case "WARNING": return "glyphicon-warning-sign";
        case "SUCCESS": return "glyphicon-ok-circle";
        case "INFO":
        default:        return "glyphicon-info-sign";
      }
    }
  },
  template: `
    <div id="" data-alert-id="" class="alert alert-dismissible fade in" :class="levelClass">
        <button type="button" class="close" data-dismiss="alert">
            <span>&times;</span>
        </button>
        <span class="glyphicon" :class="levelIcon"></span>
        <strong class="alert-title">{{message}}</strong> &nbsp;
        <span class="alert-description"></span>
    </div>
    `
}