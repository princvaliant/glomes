package com.glo.run

import com.glo.ndo.DataView
import com.glo.ndo.Spc
import grails.converters.JSON
import grails.util.Environment
import org.apache.batik.transcoder.Transcoder
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.commons.logging.LogFactory
import org.apache.fop.svg.PDFTranscoder

class ReportPdfProcessorController {

    def grailsApplication
    def reportPdfProcessorService
    private static final logr = LogFactory.getLog(this)

    def rend = {
    }

    def save = {

        try {
            def fname
            if (params.type == "spc") {
                def spc = Spc.get(params.id)
                fname = spc.name
            } else {
                def dataView = DataView.get(params.id)
                fname = dataView.name
            }

            InputStream stream = new ByteArrayInputStream(params.svg.getBytes());
            String dir = grailsApplication.config.glo.reportsDirectory
            java.io.File file = new java.io.File(dir + params.date)
            if (!file.exists()) {
                file.mkdir()
            }
            def s = (Environment.currentEnvironment != Environment.DEVELOPMENT) ? "\\" : "/"
            TranscoderInput input_svg_image = new TranscoderInput(stream)
            OutputStream pdf_ostream = new FileOutputStream(dir + params.date + s + fname + ".pdf")
            TranscoderOutput output_pdf_file = new TranscoderOutput(pdf_ostream)
            Transcoder transcoder = new PDFTranscoder()
            transcoder.transcode(input_svg_image, output_pdf_file)
            stream.close()
            pdf_ostream.flush()
            pdf_ostream.close()
        } catch (Exception exc) {
            logr.warn(exc.message)
        }

        render(['success': true] as JSON)
    }

    def process = {

        try {
            reportPdfProcessorService.processAll()
            render ("OK")
        } catch (Exception exc) {
            render (exc.message)
        }
    }

}
