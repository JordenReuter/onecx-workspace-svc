package org.tkit.onecx.workspace.domain.models;

import java.io.Serializable;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class Component implements Serializable {

    @Column(name = "PRODUCT_NAME")
    private String productName;

    @Column(name = "APP_ID")
    private String appId;

    @Column(name = "NAME")
    private String name;

}
