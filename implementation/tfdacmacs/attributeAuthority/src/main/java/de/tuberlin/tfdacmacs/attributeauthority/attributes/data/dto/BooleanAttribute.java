package de.tuberlin.tfdacmacs.attributeauthority.attributes.data.dto;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class BooleanAttribute extends AttributeCreationRequest<Boolean> {
    @Override
    public List<Boolean> getValues() {
        return Lists.newArrayList(true);
    }
}
