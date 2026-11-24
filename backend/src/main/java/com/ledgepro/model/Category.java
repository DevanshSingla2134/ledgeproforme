package com.ledgepro.model;

public class Category extends BaseEntity {
    private Long businessId;
    private String name;
    private String description;

    public Category() {
        super();
    }

    public Category(Long id, Long businessId, String name, String description) {
        super(id);
        this.businessId = businessId;
        this.name = name;
        this.description = description;
    }

    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
