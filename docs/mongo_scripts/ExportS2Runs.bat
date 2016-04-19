

mongoexport -h calserver06:27017 -d glo -c epiRun -q "{'code':'S2'}" --csv --fields SilaneOutPress,H2OutPress,Water1Temp,Water2Temp,N2InPress,H2InPress,HeatTapeCheck,TMGa1Temp,TMGa1Water,TMGa1Valve,TMAl1Temp,TMAl1Water,TMAl1Valve,Cp2Mg1Temp,Cp2Mg1Water,Cp2Mg1Valve,Cp2Mg2Temp,Cp2Mg2Water,Cp2Mg2Valve,PneumaticsInPress1,TEGa3Temp,TEGa3Water,TEGa3Valve,TMIn2Temp,TMIn2Water,TMIn2Valve,N2BulkOutPress,N2ProcessOutPress,Loop2Press,Loop2FilterCheck,CoilFlow,OscillatorA_H2O_Flow,OscillatorB_H2O_Flow,GasCenterBotFlow,GasCenterTopFlow,BottomH2OFlow,RingH2OFlow,TopH2OFlow,Loop1Press,Loop1FilterCheck,NH3OutPress,NH3_A_LeftSideWeight,NH3_A_RightSideWeight,NH3_B_LeftSideWeight,NH3_B_RightSideWeight,AlarmCheck,BaseStateCheck,EpiTTCheck,TMGa_1Weight,Cp2Mg_1Weight,Cp2Mg_2Weight,TMAl_1Weight,TMIn_2Weight,TEGa_3Weight,RecipeUploaded,DLGUploaded,MDBUploaded,EpiSetupDBUpdated,GeneratorFlashRed,runNumber2,RunPictures,ReactorVacuum,dateCreated -o S2.csv