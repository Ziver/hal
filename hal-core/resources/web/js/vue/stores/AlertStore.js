import { reactive } from 'vue'

export const alertStore = reactive({
    loading: false,
    alerts: [],
    pollTimer: null,

    enableAutoLoad(enabled = true) {
        if (enabled && this.pollTimer !== null) {
            this.pollTimer = setInterval(function() {
                load();
            }, 3000);
        } else {
            clearInterval(this.timer);
            this.pollTimer == null;
        }
    },

    load() {
        fetch('/api/alert?action=poll')
            .then(response => response.json())
            .then(data => {
                data.forEach(alert => {
                    alert.source = "server";
                    alertStore.alerts.push(alert);
                });
            });
    },

    setLoading(l = true) {
        this.loading = l;
    },

    addAlertInfo(message) {
        this.alerts.push({source: "local", level: "info", message: message});
    },
    addAlertSuccess(message) {
        this.alerts.push({source: "local", level: "success", message: message});
    },
    addAlertWarning(message) {
        this.alerts.push({source: "local", level: "warning", message: message});
    },
    AddAlertError(message) {
        this.alerts.push({source: "local", level: "danger", message: message});
    },
});