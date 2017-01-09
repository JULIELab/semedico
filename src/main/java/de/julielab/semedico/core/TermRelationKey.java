package de.julielab.semedico.core;



public class TermRelationKey {
	private String startId;
	private String endId;
	private String relationType;
	
	public TermRelationKey(String startId, String endId, String incomingType) {
		this.startId = startId;
		this.endId = endId;
		this.relationType = incomingType;
	}

	public String getRelationType() {
		return relationType;
	}

	public String getStartId() {
		return startId;
	}

	public String getEndId() {
		return endId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((relationType == null) ? 0 : relationType.hashCode());
		result = prime * result
				+ ((startId == null) ? 0 : startId.hashCode());
		result = prime * result
				+ ((endId == null) ? 0 : endId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TermRelationKey other = (TermRelationKey) obj;
		if (relationType != other.relationType)
			return false;
		if (startId == null) {
			if (other.startId != null)
				return false;
		} else if (!startId.equals(other.startId))
			return false;
		if (endId == null) {
			if (other.endId != null)
				return false;
		} else if (!endId.equals(other.endId))
			return false;
		return true;
	}

}
