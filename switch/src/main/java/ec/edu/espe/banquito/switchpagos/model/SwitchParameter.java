package ec.edu.espe.banquito.switchpagos.model;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "SWITCH_PARAMETER")
public class SwitchParameter {

    @Id
    @Column(name = "code", nullable = false, length = 50)
    private String code; // String PK.

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "value_string", length = 255)
    private String valueString;

    @Column(name = "data_type", length = 50)
    private String dataType;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "last_update")
    private LocalDateTime lastUpdate;

    public SwitchParameter() {
    }

    public SwitchParameter(String code) {
        this.code = code;
    }

    // Accessors.

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValueString() {
        return valueString;
    }

    public void setValueString(String valueString) {
        this.valueString = valueString;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SwitchParameter that = (SwitchParameter) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(code);
    }

    @Override
    public String toString() {
        return "SwitchParameter{" +
                "code='" + code + '\'' +
                ", valueString='" + valueString + '\'' +
                ", dataType='" + dataType + '\'' +
                '}';
    }
}
