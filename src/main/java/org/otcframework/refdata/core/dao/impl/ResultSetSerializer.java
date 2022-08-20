/**
* Copyright (c) eTree Technologies
*
* @author  Franklin Abel
* @version 1.0
* @since   2020-06-08 
*
* This file is part of the etree-ref-data.
* 
*  The OTC framework is free software: you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation, version 3 of the License.
*
*  The OTC framework is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU General Public License for more details.
*
*  A copy of the GNU General Public License is made available as 'License.md' file, 
*  along with OTC framework project.  If not, see <https://www.gnu.org/licenses/>.
*
*/
package org.otcframework.refdata.core.dao.impl;

import java.io.IOException;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ResultSetSerializer extends JsonSerializer<ResultSet> {
	public static class ResultSetSerializerException extends JsonProcessingException {
		private static final long serialVersionUID = -914957626413580734L;

		public ResultSetSerializerException(Throwable cause) {
			super(cause);
		}
	}

	@Override
	public Class<ResultSet> handledType() {
		return ResultSet.class;
	}

	@Override
	public void serialize(ResultSet rs, JsonGenerator jsonGen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
		try {
			ResultSetMetaData rsmd = rs.getMetaData();
			int numColumns = rsmd.getColumnCount();
			String[] columnNames = new String[numColumns];
			int[] columnTypes = new int[numColumns];
			for (int i = 0; i < columnNames.length; i++) {
				columnNames[i] = rsmd.getColumnLabel(i + 1);
				columnTypes[i] = rsmd.getColumnType(i + 1);
			}
			jsonGen.writeStartArray();
			while (rs.next()) {
				boolean b;
				long l;
				double d;
				jsonGen.writeStartObject();
				for (int i = 0; i < columnNames.length; i++) {
					jsonGen.writeFieldName(columnNames[i]);
					switch (columnTypes[i]) {
					case Types.INTEGER:
						l = rs.getInt(i + 1);
						if (rs.wasNull()) {
							jsonGen.writeNull();
						} else {
							jsonGen.writeNumber(l);
						}
						break;
					case Types.BIGINT:
						l = rs.getLong(i + 1);
						if (rs.wasNull()) {
							jsonGen.writeNull();
						} else {
							jsonGen.writeNumber(l);
						}
						break;
					case Types.DECIMAL:
					case Types.NUMERIC:
						jsonGen.writeNumber(rs.getBigDecimal(i + 1));
						break;
					case Types.FLOAT:
					case Types.REAL:
					case Types.DOUBLE:
						d = rs.getDouble(i + 1);
						if (rs.wasNull()) {
							jsonGen.writeNull();
						} else {
							jsonGen.writeNumber(d);
						}
						break;
					case Types.NVARCHAR:
					case Types.VARCHAR:
					case Types.LONGNVARCHAR:
					case Types.LONGVARCHAR:
						jsonGen.writeString(rs.getString(i + 1));
						break;
					case Types.BOOLEAN:
					case Types.BIT:
						b = rs.getBoolean(i + 1);
						if (rs.wasNull()) {
							jsonGen.writeNull();
						} else {
							jsonGen.writeBoolean(b);
						}
						break;
					case Types.BINARY:
					case Types.VARBINARY:
					case Types.LONGVARBINARY:
						jsonGen.writeBinary(rs.getBytes(i + 1));
						break;
					case Types.TINYINT:
					case Types.SMALLINT:
						l = rs.getShort(i + 1);
						if (rs.wasNull()) {
							jsonGen.writeNull();
						} else {
							jsonGen.writeNumber(l);
						}
						break;
					case Types.DATE:
						provider.defaultSerializeDateValue(rs.getDate(i + 1), jsonGen);
						break;
					case Types.TIMESTAMP:
						provider.defaultSerializeDateValue(rs.getTime(i + 1), jsonGen);
						break;
					case Types.BLOB:
						Blob blob = rs.getBlob(i);
						provider.defaultSerializeValue(blob.getBinaryStream(), jsonGen);
						blob.free();
						break;
					case Types.CLOB:
						Clob clob = rs.getClob(i);
						provider.defaultSerializeValue(clob.getCharacterStream(), jsonGen);
						clob.free();
						break;
					case Types.ARRAY:
						throw new RuntimeException("ResultSetSerializer not yet implemented for SQL type ARRAY");
					case Types.STRUCT:
						throw new RuntimeException("ResultSetSerializer not yet implemented for SQL type STRUCT");
					case Types.DISTINCT:
						throw new RuntimeException("ResultSetSerializer not yet implemented for SQL type DISTINCT");
					case Types.REF:
						throw new RuntimeException("ResultSetSerializer not yet implemented for SQL type REF");
					case Types.JAVA_OBJECT:
					default:
						provider.defaultSerializeValue(rs.getObject(i + 1), jsonGen);
						break;
					}
				}
				jsonGen.writeEndObject();
			}
			jsonGen.writeEndArray();
		} catch (SQLException e) {
			throw new ResultSetSerializerException(e);
		}
	}
}
