

Ext.define('glo.controller.TopnavPanel', {
	extend : 'Ext.app.Controller',

	views : ['topnav.Panel','main.Panel'],

	refs : [{
				ref : 'topnavPanel',
				selector : 'topnavpanel'
			}],
	
	init : function() {

		this.control({
					'topnavpanel' : {
						afterrender : this.onPanelRendered
					},
					'topnavpanel button[action=main]' : {
						click : this.onMainClicked
					},
					'topnavpanel button[action=home]' : {
						click : this.onHomeClicked
					},
					'topnavpanel button[action=e10]' : {
						click : this.onE10Clicked
					},
					'topnavpanel button[action=report]' : {
						click : this.onReportClicked
					},
					'topnavpanel button[action=spc]' : {
						click : this.onSpcClicked
					},
                    'topnavpanel button[action=dataentryrun]' : {
                        click : this.onDataEntryClicked
                    },
					'topnavpanel button[action=config]' : {
						click : this.onConfigClicked
					},
					'topnavpanel button[action=dashboard]' : {
						click : this.onDashboardClicked
					},
					'topnavpanel button[action=logout]' : {
						click : this.onLogoutClicked
					}
				});
		
	},
	
	onPanelRendered : function() {
		
		var s = document.URL.split("?")
		if (s.length == 2) {
			switch  (s[1]) {
			case 'report':
				this.onDashboardClicked();
				break;
			case 'equipment':
				this.onE10Clicked();
				break
			default:
				this.onMainClicked();	
			}
		} else {
			this.onMainClicked();
		}
	},

	onMainClicked : function(b, e) {

		if (!Ext.getCmp('mainpanel')) {

			var tb = Ext.create('glo.view.main.Panel', {
						id : "mainpanel"
					});

			this.getTopnavPanel().add(tb);
		}

		this.getTopnavPanel().layout.setActiveItem("mainpanel");
	 },
	 
	onHomeClicked : function(b, e) {

		window.location.href = rootFolder + '/';
	},
	
	onReportClicked : function(b, e) {
		
		if (!Ext.getCmp('dataViewPanel')) {

			var tb = Ext.create('glo.view.dataView.Panel', {
						id : "dataViewPanel"
					});

			this.getTopnavPanel().add(tb);
		}

		this.getTopnavPanel().layout.setActiveItem("dataViewPanel");
	},	
	
	onSpcClicked : function(b, e) {

		if (!Ext.getCmp('spcPanel')) {

			var tb = Ext.create('glo.view.spc.Panel', {
						id : "spcPanel"
					});

			this.getTopnavPanel().add(tb);
		}

		this.getTopnavPanel().layout.setActiveItem("spcPanel");
	},	
	
	onE10Clicked : function(b, e) {

		if (!Ext.getCmp('equipmentpanel')) {

			var tb = Ext.create('glo.view.equipment.Panel', {
						id : "equipmentpanel"
					});

			this.getTopnavPanel().add(tb);
		}

		this.getTopnavPanel().layout.setActiveItem("equipmentpanel");
	},	

	onDataEntryClicked : function(b, e) {

        if (!Ext.getCmp('dataentrypanel')) {

            var tb = Ext.create('glo.view.dataEntry.Panel', {
                id : "dataentrypanel"
            });

            this.getTopnavPanel().add(tb);
        }

        this.getTopnavPanel().layout.setActiveItem("dataentrypanel");
    },

    onConfigClicked : function(b, e) {
		
		if (!Ext.getCmp('configurationpanel')) {

			var tb = Ext.create('glo.view.configuration.Panel', {
						id : "configurationpanel"
					});

			this.getTopnavPanel().add(tb);
		}

		this.getTopnavPanel().layout.setActiveItem("configurationpanel");
	},	
	
	onDashboardClicked : function(b, e) {
		
		if (!Ext.getCmp('dashboardPanel')) {

			var tb = Ext.create('glo.view.dashboard.Panel', {
						id : "dashboardPanel"
					});

			this.getTopnavPanel().add(tb);
		}

		this.getTopnavPanel().layout.setActiveItem("dashboardPanel");
	},	
	
	onLogoutClicked : function(b, e) {

		Ext.Ajax.request({
			scope : this,
			url : rootFolder + 'logout',
			success : function(response) {
				window.location.href = rootFolder + '/';
			},
			failure : function(response) {
				window.location.href = rootFolder + '/';
			}
		});
	}
});