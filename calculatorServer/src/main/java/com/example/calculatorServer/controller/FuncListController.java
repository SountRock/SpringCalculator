package com.example.calculatorServer.controller;

import com.example.calculatorServer.domain.funcvar.FuncVar;
import com.example.calculatorServer.repository.FuncVarRepository;
import com.example.calculatorServer.service.ImplService.FuncVarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("list")
public class FuncListController implements SaveDocument<FuncVar> {
    @Autowired
    private FuncVarService service;

    @PostMapping("add")
    public ResponseEntity addFunc(@RequestBody FuncVar funcVar){
        service.addFunc(funcVar);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("calculateWithName/{name}/{expression}")
    public ResponseEntity<String> calculateWithName(@PathVariable("expression") String expression, @PathVariable("name") String name){
        FuncVar temp = new FuncVar(expression);
        temp.setCreateDate(LocalDateTime.now());
        temp.setName(name);

        ResponseEntity<String> result = service.calculateFunction(temp);
        return result;
    }

    @GetMapping("history")
    public ResponseEntity<List<FuncVar>> getHistory(){
        return service.getHistory();
    }

    @GetMapping("findByName/{name}")
    public ResponseEntity<List<FuncVar>> findByName(@PathVariable("name") String name){
        return new ResponseEntity<>(service.getFuncByName(name), HttpStatus.OK);
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity deleteById(@PathVariable("id") Long id){
        return service.deleteById(id);
    }

    @DeleteMapping("deleteByName/{name}")
    public ResponseEntity deleteByName(@PathVariable("name") String name){
        return service.deleteByName(name);
    }

    @PostMapping("saveList/{directory}/{fileName}")
    public ResponseEntity saveList(@PathVariable("directory") String directory, @PathVariable("fileName") String fileName) {
        try {
            boolean isSave = saveDocument(directory, fileName, "FL", service.getHistory().getBody());
            if (isSave) {
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("loadList/{directory}/{fileName}")
    public ResponseEntity loadList(@PathVariable("directory") String directory, @PathVariable("fileName") String fileName){
        List<FuncVar> loadList = service.loadDocument(directory, fileName);

        if(loadList != null){
            service.loadFuncs(loadList);
            return new ResponseEntity<>(loadList, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @GetMapping("showFiles/{directory}")
    public ResponseEntity<List<String>> showFilesInDirectory(@PathVariable("directory") String directory){
        List<String> files = showFiles(directory);
        return new ResponseEntity<>(files, HttpStatus.OK);
    }

    public FuncVarRepository getRepo(){
        return service.getFuncRepo();
    }
}
