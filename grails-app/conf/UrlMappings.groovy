class UrlMappings {

	
		static mappings = {
	
			"/$controller/$action?/$id?"{ constraints {
					// apply constraints here
				} }
	
			"/"(view:"/index")
			"500"(view:'/error')
	
	
			// Equipments rest services ------------------------------------------------------------------------
			"/inv" (controller:"inv"){
				action = [GET:"list", POST: "create"]
			}
	
			//	"/activiti" (controller:"activiti"){
			//		action = [GET:"test"]
			//	}
	
			// User related rest services ------------------------------------------------------------------------
			"/logins" (controller:"userRest"){
				action = [POST:"login"]
			}
			"/logouts" (controller:"userRest"){
				action = [POST:"logout"]
			}
	
	
			// Taskbook rest services ------------------------------------------------------------------------
			"/transitions" (controller:"transitions"){
				action = [GET:"getTransitions",POST:"executeTransition"]
			}
	
			// Unit rest services ------------------------------------------------------------------------
			"/units" (controller:"units"){
				action = [GET:"list", POST: "create"]
			}
			"/units/$id" (controller:"units"){
				action = [GET:"get", PUT:"update",DELETE:"delete"]
			}
			"/units/updates" (controller:"units"){
				action = [POST: "updates"]
			}
			"/units/move" (controller:"units"){
				action = [POST: "move"]
			}
			"/units/loss" (controller:"units"){
				action = [POST: "loss"]
			}
			"/units/bonus" (controller:"units"){
				action = [POST: "bonus"]
			}
			"/units/rework" (controller:"units"){
				action = [POST: "rework"]
			}
			"/units/autoRoute" (controller:"units"){
				action = [GET: "autoRoute"]
			}
			"/units/split" (controller:"units"){
				action = [POST: "split"]
			}
            "/units/children" (controller:"units"){
                action = [GET: "children"]
            }
            "/units/parent" (controller:"units"){
                action = [GET: "parent"]
            }
			"/units/merge" (controller:"units"){
				action = [POST: "merge"]
			}
			"/units/unmerge" (controller:"units"){
				action = [POST:"unMerge"]
			}
			"/units/bcCapabilities" (controller:"units"){
				action = [GET: "barcodeCapabilities"]
			}
			"/units/printer" (controller:"units"){
				action = [POST: "barcodePrint"]
			}
			"/units/scanner" (controller:"units"){
				action = [GET: "barcodeScan"]
			}
			"/units/fileUpload" (controller:"units"){
				action = [POST: "fileUpload"]
			}
            "/units/imageUpload" (controller:"units"){
                action = [POST: "imageUpload"]
            }
            "/units/saveInstruction" (controller:"units"){
                action = [POST: "saveInstruction"]
            }
			"/units/validate" (controller:"units"){
				action = [POST: "validate"]
			}
			"/units/revive" (controller:"units"){
				action = [POST: "revive"]
			}
			"/units/addNote" (controller:"units"){
				action = [POST: "addNote"]
			}
            "/units/updateProduct" (controller:"units"){
                action = [POST: "updateProduct"]
            }
            "/units/mergeTestIndexes" (controller:"units"){
                action = [GET: "mergeTests"]
            }
			"/units/RRRELOADDD" (controller:"units"){
				action = [GET: "RRRELOADDD"]
			}
			"/units/RRRELOADDDHIST" (controller:"units"){
				action = [GET: "RRRELOADDDHIST"]
			}
	
			// Products rest services ------------------------------------------------------------------------
			"/productCompanies" (controller:"productCompanies"){
				action = [GET:"list"]
			}
			"/productCompanies/vendorCode" (controller:"productCompanies"){
				action = [GET:"listByVendorCode"]
			}
			"/products" (controller:"products"){
				action = [GET:"list"]
			}
            "/productFamilies" (controller:"productFamilies"){
                    action = [GET:"list"]
            }
	
			// Locations rest services ------------------------------------------------------------------------
			"/locations" (controller:"locations"){
				action = [GET:"list"]
			}
			"/locations/test" (controller:"locations"){
				action = [GET:"test"]
			}
			
			// Workcenters rest services ----------------------------------------------------------------------
			"/workCenters" (controller:"workCenters"){
				action = [GET:"list"]
			}
	
			// Equipments rest services ------------------------------------------------------------------------
			"/equipments" (controller:"equipments"){
				action = [GET:"list"]
			}
			"/equipments/getAll" (controller:"equipments"){
				action = [GET:"getAll", POST:"add"]
			}
			"/equipments/getAll/$id" (controller:"equipments"){
				action = [PUT:"update",DELETE:"delete"]
			}
			"/equipments/getFields" (controller:"equipments"){
				action = [GET:"getFields"]
			}
			"/equipments/status" (controller:"equipments"){
				action = [POST:"statusChange"]
			}
			"/equipments/chart" (controller:"equipments"){
				action = [GET:"chart"]
			}
			"/equipments/getStatuses" (controller:"equipments"){
				action = [GET:"getStatuses"]
			}
            "/equipments/currentStatus" (controller:"equipments"){
                action = [GET:"getCurrentStatus",POST:"getCurrentStatus"]
            }
			"/equipments/failures" (controller:"equipments"){
				action = [GET:"getFailures"]
			}
			"/equipments/unscheduled" (controller:"equipments"){
				action = [GET:"getUnscheduled"]
			}
			"/equipments/comment" (controller:"equipments"){
				action = [POST:"editComment"]
			}
			"/equipments/maintenance" (controller:"equipments"){
				action = [GET:"getMaintenance",POST:"editMaintenance"]
			}
			
			"/metaItems" (controller:"metaItems"){
				action = [GET:"list"]
			}

            // Data entry rest services ------------------------------------------------------------------------
            "/dataEntry" (controller:"dataEntry"){
                action = [GET:"list"]
            }
            "/dataEntry/get" (controller:"dataEntry"){
                action = [GET:"get"]
            }
            "/dataEntry/getAll" (controller:"dataEntry"){
                action = [GET:"getAll", POST:"save"]
            }
            "/dataEntry/getListFields" (controller:"dataEntry"){
                action = [GET:"getListFields"]
            }
            "/dataEntry/getListData" (controller:"dataEntry"){
                action = [GET:"getListData"]
            }
            "/dataEntry/getContentData" (controller:"dataEntry"){
                action = [GET:"getContentData"]
            }
            "/dataEntry/getContentData/$id" (controller:"dataEntry"){
                action = [DELETE:"delete"]
            }
            "/dataEntry/copy" (controller:"dataEntry"){
                action = [POST:"copy"]
            }
			
			// Data views rest services ------------------------------------------------------------------------
			"/dataViews" (controller:"dataViews"){
				action = [GET:"list", POST:"save"]
			}
			"/dataViews/$id" (controller:"dataViews"){
				action = [GET:"get",DELETE:"delete"]
			}
			"/dataViews/duplicate" (controller:"dataViews"){
				action = [POST:"duplicate"]
			}
			"/dataViews/getFields" (controller:"dataViews"){
				action = [GET:"getFields"]
			}
			"/dataViews/export" (controller:"dataViews"){
				action = [GET:"export"]
			}
			"/dataViews/joins" (controller:"dataViews"){
				action = [GET:"joins",POST:"updateJoin"]
			}
			"/dataViews/removeJoin" (controller:"dataViews"){
				action = [DELETE:"removeJoin"]
			}
			"/dataViews/reorderVariables" (controller:"dataViews"){
				action = [POST:"reorderVariables"]
			}
			"/dataViews/attachVariable" (controller:"dataViews"){
				action = [POST:"attachVariable"]
			}
			"/dataViews/addFormulaVariable" (controller:"dataViews"){
				action = [POST:"addFormulaVariable"]
			}
			"/dataViews/saveFormulaVariable" (controller:"dataViews"){
				action = [POST:"saveFormulaVariable"]
			}
			"/dataViews/importExcel" (controller:"dataViews"){
				action = [POST: "importExcel"]
			}
			"/dataViews/removeFromDashboard" (controller:"dataViews"){
				action = [POST:"removeDataViewFromDashboard"]
			}
			"/dataViews/dashboards" (controller:"dataViews"){
				action = [GET:"getDashboards", POST:"saveDashboards"]
			}
			
			"/dataViewCharts" (controller:"dataViewCharts"){
				action = [GET:"getDataViewCharts"]
			}
			"/dataViewCharts/saveVariable" (controller:"dataViewCharts"){
				action = [POST:"saveVariable"]
			}
			"/dataViewCharts/deleteVariable" (controller:"dataViewCharts"){
				action = [POST:"deleteVariable"]
			}
			"/dataViewCharts/changeUrl" (controller:"dataViewCharts"){
				action = [POST:"changeUrl"]
			}
			"/dataViewCharts/fields" (controller:"dataViewCharts"){
				action = [GET:"getFields"]
			}
			"/dataViewCharts/draw" (controller:"dataViewCharts"){
				action = [GET:"drawChart"]
			}
			
			// Equipments rest services ------------------------------------------------------------------------
			"/operations" (controller:"operations"){
				action = [GET:"list"]
			}
			
			// Taskbook rest services ------------------------------------------------------------------------
			"/taskbooks/processSummary" (controller:"taskbooks"){
				action = [GET:"processSummary"]
			}
			"/taskbooks/taskNameSummary" (controller:"taskbooks"){
				action = [GET:"taskNameSummary"]
			}
			"/taskbooks/startProcesses" (controller:"taskbooks"){
				action = [GET:"startProcesses"]
			}
			"/taskbooks/diagram" (controller:"taskbooks"){
				action = [GET:"diagram"]
			}
			"/taskbooks/unitTasks" (controller:"taskbooks"){
				action = [GET:"unitTasks"]
			}
			"/taskbooks/taskDefs" (controller:"taskbooks"){
				action = [GET:"taskDefs"]
			}
			"/taskbooks/taskDefsCompleted" (controller:"taskbooks"){
				action = [GET:"taskDefsCompleted"]
			}
			"/taskbooks/procDefs" (controller:"taskbooks"){
				action = [GET:"procDefs"]
			}
			"/taskbooks/processes" (controller:"taskbooks"){
				action = [GET:"processes"]
			}
			"/taskbooks/procDefsCompleted" (controller:"taskbooks"){
				action = [GET:"procDefsCompleted"]
			}
			
			// Variables rest services ------------------------------------------------------------------------
			"/variables/processCategory/$id" (controller:"variables"){
				action = [GET:"getVariablesPerProcessCategory",DELETE:"deleteNode"]
			}
			"/variables/processCategorySpc/$id" (controller:"variables"){
				action = [GET:"getVariablesPerProcessCategorySpc",DELETE:"deleteNode"]
			}
			"/variables/dataView" (controller:"variables"){
				action = [GET:"getVariablesPerDataView"]
			}
			"/variables/dataView/$id" (controller:"variables"){
				action = [PUT:"saveTitle",DELETE:"deleteVariable",POST:"editVariable"]
			}
	
	
			// Companies rest services ------------------------------------------------------------------------
			"/companies" (controller:"companies"){
				action = [GET:"list", POST: "create"]
			}
	
			// Users rest services ------------------------------------------------------------------------
			"/users" (controller:"users"){
				action = [GET:"list", POST: "create"]
			}
			"/users/hasRole" (controller:"users"){
				action = [GET:"hasRole"]
			}
				
			// Contents rest services
			"/contents" (controller:"contents"){
				action = [GET:"listByTask", POST: "create"]
			}
			"/contents/first/dc" (controller:"contents"){
				action = [GET:"listFirstTaskDc"]
			}
			"/contents/first/form/dc" (controller:"contents"){
				action = [GET:"formFirstTaskDc"]
			}
			"/contents/din" (controller:"contents"){
				action = [GET:"listInDin"]
			}
			"/contents/dc" (controller:"contents"){
				action = [GET:"listDc"]
			}
			"/contents/comboData" (controller:"contents"){
				action = [GET:"comboData"]
			}
	
			"/notes" (controller:"notes"){
				action = [GET:"list"]
			}
			"/files" (controller:"files"){
				action = [GET:"list"]
			}
			"/files/download" (controller:"files"){
				action = [GET:"download"]
			}
			"/yieldLosses" (controller:"yieldLossReasons"){
				action = [GET:"list"]
			}
			"/yieldLosses/fields" (controller:"yieldLossReasons"){
				action = [GET:"getFields"]
			}
			"/bonuses" (controller:"bonusReasons"){
				action = [GET:"list"]
			}
			"/bonuses/fields" (controller:"bonusReasons"){
				action = [GET:"getFields"]
			}
			"/reworks" (controller:"reworkReasons"){
				action = [GET:"list"]
			}
			"/reworks/fields" (controller:"reworkReasons"){
				action = [GET:"getFields"]
			}
			"/wafer/map" (controller:"wafer"){
				action = [GET:"getMap"]
			}
			"/wafer/chart" (controller:"wafer"){
				action = [POST:"getChart"]
			}
			"/wafer/parameters" (controller:"wafer"){
				action = [GET:"getParameters"]
			}
			"/wafer/parameters2" (controller:"wafer"){
				action = [GET:"getParameters2"]
			}
			"/wafer/export" (controller:"wafer"){
				action = [POST:"export"]
			}
			"/wafer/filters" (controller:"wafer"){
				action = [GET:"getFilters"]
			}
            "/wafer/summaries" (controller:"wafer"){
                action = [GET:"getSummaries"]
            }
            "/wafer/addSummary" (controller:"wafer"){
                action = [POST: "addSummary"]
            }
            "/wafer/deleteSummary" (controller:"wafer"){
                action = [POST: "deleteSummary"]
            }
            "/testDataImages/index" (controller:"testDataImages"){
                action = [GET: "index"]
            }
            "/testDataImages/save" (controller:"testDataImages"){
                action = [POST: "save"]
            }
			"/wafer/adminFilters" (controller:"wafer"){
				action = [GET:"getAdminFilters"]
			}
			"/wafer/specFilters" (controller:"wafer"){
				action = [GET:"getSpecFilters"]
			}
			"/wafer/addFilter" (controller:"wafer"){
				action = [POST: "addFilter"]
			}
			"/wafer/deleteFilter" (controller:"wafer"){
				action = [POST:"deleteFilter"]
			}
			"/wafer/selectFilter" (controller:"wafer"){
				action = [POST:"selectFilter"]
			}
			"/wafer/specs" (controller:"wafer"){
				action = [GET:"getSpecs", POST:'addSpec']
			}
            "/wafer/comment" (controller:"wafer"){
                action = [GET:"getComment", POST:"editComment"]
            }
			"/wafer/specData" (controller:"wafer"){
				action = [GET:'specData']
			}
			"/spc/chart" (controller:"spc"){
				action = [POST:"getChart"]
			}
			"/spc" (controller:"spc"){
				action = [GET:"list", POST:"save"]
			}
			"/spc/$id" (controller:"spc"){
				action = [GET:"get",DELETE:"delete"]
			}
			"/spc/duplicate" (controller:"spc"){
				action = [POST:"duplicate"]
			}
			"/spc/getFields" (controller:"spc"){
				action = [GET:"getFields"]
			}
			"/spc/attachVariable" (controller:"spc"){
				action = [POST:"attachVariable"]
			}
			"/spc/removeVariable" (controller:"spc"){
				action = [POST:"removeVariable"]
			}
			"/spc/editVariable" (controller:"spc"){
				action = [POST:"editVariable"]
			}
            "/spc/getVariable" (controller:"spc"){
                action = [GET:"getVariable"]
            }
			"/spc/comment" (controller:"spc"){
				action = [GET:"getComments", POST:"addComment"]
			}
            "/spc/sss" (controller:"spc"){
                action = [GET:"sss"]
            }
			"/probeTest/devices" (controller:"probeTest"){
				action = [GET:"getDevices"]
			}
			"/probeTest/device" (controller:"probeTest"){
				action = [GET:"get"]
			}
            "/probeTest/export" (controller:"probeTest"){
                action = [POST:"exportData"]
            }
			"/relTest/currents" (controller:"relTest"){
				action = [GET:"getCurrents"]
			}
			"/relTest/hours" (controller:"relTest"){
				action = [GET:"getHours"]
			}
			"/relTest/charts" (controller:"relTest"){
				action = [GET:"getCharts"]
			}
			"/relTest/chartsIV" (controller:"relTest"){
				action = [GET:"getChartsIV"]
			}
			"/relTest/devicesByGroup" (controller:"relTest"){
				action = [GET:"getDevicesByGroup"]
			}
			"/relTest/export" (controller:"relTest"){
				action = [GET:"export"]
			}
			
			"/lampTest/currents" (controller:"lampTest"){
				action = [GET:"getCurrents"]
			}
			"/lampTest/steps" (controller:"lampTest"){
				action = [GET:"getSteps"]
			}
			"/lampTest/devicesByGroup" (controller:"lampTest"){
				action = [GET:"getDevicesByGroup"]
			}
			"/lampTest/charts1" (controller:"lampTest"){
				action = [GET:"getCharts1"]
			}
			"/lampTest/charts2" (controller:"lampTest"){
				action = [GET:"getCharts2"]
			}
			
			"/import/d65" (controller:"import"){
				action = [POST:"d65"]
			}
			"/epiParser" (controller:"epiParser"){
				action = [GET:"index"]
			}
            "/reportPdfProcessor/save" (controller:"reportPdfProcessor"){
                action = [POST:"save"]
            }
            "/reportPdfProcessor/rend" (controller:"reportPdfProcessor"){
                action = [GET: "rend"]
            }
            "/softwareRequest/create" (controller:"softwareRequest"){
                action = [POST: "create"]
            }
		}
	}
	