package org.astarteplatform.devicesdk.generic;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "persistent_properties")
public class AstarteGenericPropertyEntry {
  public static final String INTERFACE_FIELD_NAME = "interface";

  @DatabaseField(id = true, canBeNull = false)
  private String id;

  @DatabaseField(columnName = INTERFACE_FIELD_NAME, canBeNull = false)
  private String interfaceName;

  @DatabaseField(canBeNull = false)
  private String path;

  @DatabaseField(canBeNull = false, dataType = DataType.BYTE_ARRAY)
  private byte[] bsonValue;

  AstarteGenericPropertyEntry() {
    // Needed by ORMLite
  }

  AstarteGenericPropertyEntry(String interfaceName, String path, byte[] bsonValue) {
    this.id = interfaceName + "/" + path;
    this.interfaceName = interfaceName;
    this.path = path;
    this.bsonValue = bsonValue;
  }

  public String getId() {
    return id;
  }

  public String getInterfaceName() {
    return interfaceName;
  }

  public byte[] getBSONValue() {
    return bsonValue;
  }

  public String getPath() {
    return path;
  }
}
