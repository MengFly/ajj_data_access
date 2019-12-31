package com.akxy.entity;

public class PointSign {
	
	public String tunnelName;
	
	public Double depth;
	
	public Double distance;

	public Double getDepth() {
		return depth;
	}

	public void setDepth(Double depth) {
		this.depth = depth;
	}

	public Double getDistance() {
		return distance;
	}

	public void setDistance(Double distance) {
		this.distance = distance;
	}

	public String getTunnelName() {
		return tunnelName;
	}

	public void setTunnelName(String tunnelName) {
		this.tunnelName = tunnelName;
	}

	@Override
	public String toString() {
		return "PointSign [tunnelName=" + tunnelName + ", depth=" + depth + ", distance=" + distance + "]";
	}
	
}
