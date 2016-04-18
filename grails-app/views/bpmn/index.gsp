
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="mainext" />
<meta name="layout" content="mainext" />
<link rel="stylesheet" href="${resource(dir:'js/ext/resources/css',file:'ext-all.css')}" />
<link rel="stylesheet" href="${resource(dir:'js/ext/resources/css',file:'icons.css')}" />
<g:javascript src="ext/ext-all.js"></g:javascript>


</head>
<body>
	<!-- Page commands -->
	<div id="pagetitle">
		<div class="wrapper">
			<div class="nav">
				<h1>
					<g:message code="navigation.glo.admin" />
				</h1>
			</div>
		</div>
	</div>
	<!-- End of Page Commands -->

	<div id="pageBody">
		<div class="wrapper">
			
			<script>


			Ext.onReady(function() {

				
			    var sencha = Ext.create('Ext.draw.Component', {
			        width: 120,
			        height: 60,
			        draggable: true,
			        floating:true,
			        resizable: {
			            dynamic: true,
			            pinned: false
			        },
			        renderTo: Ext.getBody(),
			        gradients: [{
			            id: 'grad1',
			            angle: 125,
			            stops: {
			                0: {
			                    color: '#AACE36'
			                },
			                100: {
			                    color: '#2FA042'
			                }
			            }
			        }],
			        items: [ {
			                type: 'rect',
			                width: 120,
			                height: 60,
			                radius: 10,
			                opacity: 0.2,
			                stroke: 'blue',
			                'stroke-width': 1,
			           		 fill: 'url(#grad1)'
			        },{
			            type: "text",
			            x: 5,
			            y: 30,
			            maxWidth: 110,
			            text: "Hello, Spriteasd !",
			            fill: "blue",
			            font: "14px tahoma"
			        }]
			    });

			    Ext.create('Ext.form.FormPanel', {
				    x:20,
				    y:30,
				    width:200,
				    height:80,
				    floating: true,
				    draggable: {
						constrain:true
				    },
				    resizable:{ 
				    	constrain:true
					},
				    id: 'taskForm',
			        layout: 'fit',
			        bodyPadding: 0,
			        renderTo   : Ext.getBody(),
			        items: [{
			            xtype     : 'textarea',
			            padding: 0,
			            margin: 0,
			            name      : 'message',
			            anchor    : '100%'
			        }]
			    });


			    
			});







			</script>
			
			
			
		</div>
	</div>
</body>
</html>