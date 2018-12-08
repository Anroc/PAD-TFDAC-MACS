package de.tuberlin.tfdacmacs.attributeauthority.attributes.data.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.tuberlin.tfdacmacs.basics.attributes.data.AttributeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.EXISTING_PROPERTY, property="type")
@JsonSubTypes({
        @JsonSubTypes.Type(value=BooleanAttribute.class, name="BOOLEAN"),
        @JsonSubTypes.Type(value=StringAttribute.class, name="STRING"),
        @JsonSubTypes.Type(value=NumberAttribute.class, name="NUMBER")
})
public abstract class AttributeCreationRequest<T> {

    @NotBlank
    private String name;

    @NotNull
    private AttributeType type;

    public abstract List<T> getValues();
}
