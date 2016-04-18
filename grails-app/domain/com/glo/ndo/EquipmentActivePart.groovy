package com.glo.ndo

class EquipmentActivePart implements Comparable {

	static auditable = true

	Integer position

	static belongsTo = [
		equipment:Equipment,
		equipmentPart:EquipmentPart
	]

	static constraints = {
		position blank:false
	}

	String toString() {
		equipmentPart?.code + ", " + position
	}

	public int compareTo(def other) {
		return  position <=>  other?.position
	}

}
