package org.oiue.service.sql;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SqlServiceResult implements Serializable{
	private boolean result;
	private Object data;

	public boolean getResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
}
