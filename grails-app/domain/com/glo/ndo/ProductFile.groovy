package com.glo.ndo

class ProductFile {

	String name
	byte[] fileBytes
	
	static belongsTo = [
		product: Product
	]
	
	static constraints = {
		// Limit upload file size to 3MB
		fileBytes maxSize: 1024 * 1024 * 3
		name blank:false,maxSize:100
	}
	
	String toString() {
		name 
	}
}
