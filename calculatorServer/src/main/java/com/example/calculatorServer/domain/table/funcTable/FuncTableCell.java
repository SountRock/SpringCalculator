package com.example.calculatorServer.domain.table.funcTable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name ="FuncTableCell")
public class FuncTableCell {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="`id`")
    private Long id;
    @Column(name="`cellName`")
    private String cellName;
    @Column(name="`cellCount`")
    private long cellCount;

    @Column(name="`expression`")
    @Lob
    private String expression;
    @Column(name="`result`")
    private List<String> result;
    @Column(name="`resultString`")
    @Lob
    private String resultString;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funcTable_id")
    @JsonIgnore
    private FuncTable funcTable;
}
