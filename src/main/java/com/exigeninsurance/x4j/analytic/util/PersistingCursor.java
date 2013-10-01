/*
 * Copyright 2008-2013 Exigen Insurance Solutions, Inc. All Rights Reserved.
 *
*/

package com.exigeninsurance.x4j.analytic.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.exigeninsurance.x4j.analytic.api.ReportException;


public class PersistingCursor implements Cursor {
	private File file;
	private FileOutputStream fileOut;
	private ObjectOutputStream objectOut;
	private ResultSet rs;
	private final CursorMetadata metadata;

	public PersistingCursor(File file, ResultSet rs) {
		this.file = file;
		this.rs = rs;
		metadata = createMetadata(rs);
		initStreams();
		writeMetadata();
	}

	private CursorMetadata createMetadata(ResultSet rs) {
		try {
			return CursorMetadata.createFromResultSet(rs);
		} catch (SQLException e) {
			throw new ReportException(e);
		}
	}

	private void initStreams() {
		try {
			fileOut = new FileOutputStream(file);
			objectOut = new ObjectOutputStream(fileOut);
		} catch (Exception e) {
			throw new ReportException(e);
		}
	}

	private void writeMetadata() {
		checkOpen();
		try {
			objectOut.writeObject(metadata);
		} catch (IOException e) {
			close();
			throw new ReportException(e);
		}
	}

	@Override
	public boolean next() {
		boolean next = hasNext();
		if (next) {
			writeRow();
		}            
		return next;
	}



	private boolean hasNext() {
		try {
			return rs.next();
		} catch (SQLException e) {
			throw new ReportException(e);
		}
	}

	private void writeRow() {
		checkOpen();    	
		try {
			metadata.writeRow(objectOut,rs);
		} catch (IOException e) {
			throw new ReportException(e);
		} catch (SQLException e) {
			throw new ReportException(e);
		}              

	}

	@Override
	public CursorMetadata getMetadata() {
		return metadata;
	}

	@Override
	public Object getObject(int i) {
		try {
			return rs.getObject(i);
		} catch (SQLException e) {
			throw new ReportException(e);
		}
	}

	@Override
	public void reset() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() {
		try {
			if (objectOut != null) {
				objectOut.close();
			}
			if (fileOut != null) {
				fileOut.close();
			}
			fileOut = null;
		} catch (IOException e) {
			throw new ReportException(e);
		}
	}

	void checkOpen(){
		if(isClosed()){
			throw new ReportException("Cursor is closed");
		}
	}

	@Override
	public boolean isClosed() {		
		return fileOut == null;
	}
}