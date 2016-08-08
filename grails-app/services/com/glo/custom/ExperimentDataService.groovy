package com.glo.custom

import com.glo.ndo.Experiment
import org.apache.commons.logging.LogFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook


class ExperimentDataService {

	private static final logr = LogFactory.getLog(this)
	
	def managementService
	def runtimeService
	def repositoryService
	def messageSource
	def sequenceGeneratorService
    def grailsApplication
	def mongo

	def importFiles () {

        String dir = grailsApplication.config.glo.experimentDataDirectory
        File f = new File(dir)
        if (f.exists()) {
            def dirloc = f.listFiles([accept: { file -> file ==~ /.*?\.xlsx/ }] as FileFilter)?.toList()
            dirloc.sort{ a,b -> b.lastModified() <=> a.lastModified() }.each { fd ->
                if (fd.getName()[0] != '~') {
                    try {
                        XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(fd.bytes))
                        def sheet = workbook.getSheet("Runsheet")
                        if (sheet) {
                            def rowCode = sheet.getRow(2);
                            def cellCode = rowCode.getCell(2);
                            def rowName = sheet.getRow(6);
                            def cellName = rowName.getCell(2);
                            if (cellCode && cellName && cellCode.getStringCellValue() && cellName.getStringCellValue()) {
                                def experiment = Experiment.findByCode(cellCode.getStringCellValue())
                                if (!experiment) {
                                    experiment = new Experiment()
                                }
                                if (cellName.getStringCellValue().length() < 1000) {
                                    experiment.code = cellCode.getStringCellValue()
                                    experiment.name = cellName.getStringCellValue()
                                    experiment.save(failOnError: true)
                                }
                            }
                        }
                        workbook = null;
                    } catch (Exception exc) {
                        System.out.println(exc.getMessage())
                    }
                }
            }
        }
	}
}
