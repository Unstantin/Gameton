package org.example.responses;

import lombok.AllArgsConstructor;
import org.example.models.Zpot;

import java.util.List;

@AllArgsConstructor
public class ConstantEnvironmentResponse {
    public String realmName;
    public List<Zpot> zpots;
}
