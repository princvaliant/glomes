package com.glo.run

import grails.converters.JSON
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.commons.logging.LogFactory
import org.apache.batik.transcoder.image.JPEGTranscoder

class TestDataImagesController {

    def grailsApplication
    def springSecurityService
    def testDataImagesService
    def mongo

	private static final logr = LogFactory.getLog(this)

	def index = {
	}

    def save = {

        try {
            InputStream stream = new ByteArrayInputStream(params.svg.getBytes());
            String outputImagePath = grailsApplication.config.glo.imagesOutputDirectory
            String outputImageFile = outputImagePath + params.image
            TranscoderInput input_svg_image = new TranscoderInput(stream)
            OutputStream pdf_ostream = new FileOutputStream(outputImageFile)
            TranscoderOutput output_pdf_file = new TranscoderOutput(pdf_ostream)
            JPEGTranscoder transcoder = new JPEGTranscoder()
            transcoder.transcode(input_svg_image, output_pdf_file)
            stream.close()
            pdf_ostream.flush()
            pdf_ostream.close()
        } catch (Exception exc) {
            logr.warn(exc.message)
        }
        render (['success':true] as JSON)
    }

    def process = {

        try {
            testDataImagesService.processAll()
            render ("OK")
        } catch (Exception exc) {
            render (exc.message)
        }
    }

}

