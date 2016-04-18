
gloApp = Ext.create('Ext.app.Application', {
			name: 'glo',
			appFolder : fullAppFolder,
			autoCreateViewport : false,

			requires: [
                'glo.view.Viewport',
                'glo.controller.MainPanel',
                'glo.controller.EquipmentPanel',
                'glo.controller.DataViewPanel',
                'glo.controller.DataEntryPanel',
                'glo.controller.SpcPanel',
                'glo.controller.WaferPanel',
                'glo.controller.ProbeTestPanel',
                'glo.controller.RelTestPanel',
                'glo.controller.LampTestPanel',
                'glo.controller.WstDataPanel',
                'glo.controller.TopnavPanel',
                'glo.controller.DashboardPanel',
                'Ext.ux.Message',
                'Ext.ux.form.MultiSelect',
                'Ext.ux.grid.InlineRemoteMultiSort',
                'glo.controller.MeasurePanel',
                'Ext.dd.DropTarget'
            ],
		  
			controllers: [
			     'MainPanel',
			     'TbContent',
			     'EquipmentPanel',
			     'MainStartForm',
			     'DataViewPanel',
                 'DataEntryPanel',
			     'ConfigurationPanel',
			     'SpcPanel',
			     'WaferPanel',
			     'ProbeTestPanel',
			     'RelTestPanel',
			     'LampTestPanel',
			     'WstDataPanel',
			     'TopnavPanel',
			     'DashboardPanel',
			     'MeasurePanel'
			],

			launch : function() {

			    // setup the state provider, all state information will be saved
				// to a cookie
			    // Ext.state.Manager.setProvider(Ext.create('Ext.state.CookieProvider'));
				Ext.override(Ext.data.proxy.Ajax, { timeout:120000 });
				Ext.Ajax.timeout = 120000;
				
			    Ext.ns('Ext.ux.state')

				Ext.ux.state.LocalStorage = function(config){
				    Ext.ux.state.LocalStorage.superclass.constructor.call(this);
				    Ext.apply(this, config);
				    this.state = localStorage;
				};
				
				// create reusable renderer
				Ext.util.Format.comboRenderer = function(combo) {
					return function(value) {
						var record = combo.store.findRecord(combo.valueField,
								value);
						var ret = record
								? record.get(combo.displayField)
								: combo.valueNotFoundText;

						if (ret == undefined)
							ret = ''
						return ret;
					}
				}
				
				Ext.extend(Ext.ux.state.LocalStorage, Ext.state.Provider, {
				    get : function(name, defaultValue){
				        if (typeof this.state[name] == "undefined") {
				            return defaultValue
				        } else {
				            return this.decodeValue(this.state[name])
				        }
				    },
				    set : function(name, value){
				        if(typeof value == "undefined" || value === null){
				            this.clear(name);
				            return;
				        }
				        this.state[name] = this.encodeValue(value)
				        this.fireEvent("statechange", this, name, value);
				    }
				});


                var map = new Ext.util.KeyMap(document, [
                   {
                        key: "l",
                        ctrl:true,
                        fn: function(){
                            var grid = Ext.ComponentQuery.query('taskbookcontent > grid')[0];
                            if (grid !== undefined) {
                                var rows = grid.getSelectionModel().getSelection();
                                var clip = '';
                                Ext.Object.each(rows, function (record) {
                                    if (rows[record].data.code !== undefined) {
                                        if (record > 0)
                                            clip += ',';
                                        clip += rows[record].data.code;
                                    }
                                });
                                Ext.Msg.prompt('List of selected codes', '', undefined,undefined,true,clip);
                            }
                        }
                    }
                ]);

				
				if (window.localStorage) {
				    Ext.state.Manager.setProvider(new Ext.ux.state.LocalStorage())
				} else {
				    var thirtyDays = new Date(new Date().getTime()+(1000*60*60*24*60))
				    Ext.state.Manager.setProvider(new Ext.state.CookieProvider({expires: thirtyDays}))
				}

                if (isReportGenerator === false) {
				    Ext.create("glo.view.Viewport", {
					    renderTo : Ext.getBody()
				    });
                }
			}

		});
