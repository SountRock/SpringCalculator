package com.example.calculatorService.service;

import com.example.calculatorService.domain.customFunc.CustomFunction;
import com.example.calculatorService.domain.funcvar.FuncVar;
import com.example.calculatorService.domain.table.rangeTable.RangeTable;
import com.example.calculatorService.exceptions.ReferenceResultIsEmpty;
import com.example.calculatorService.exceptions.TableReferenceErrorException;
import com.example.calculatorService.repository.CustomFunctionRepository;
import com.example.calculatorService.repository.FuncVarRepository;
import com.example.calculatorService.repository.RangeTableRepository;
import com.example.calculatorService.service.MathModels.ModelCustomRightSide;
import com.example.calculatorService.service.MathModels.ModelCustomTwoSides;
import com.example.calculatorService.service.Tools.AnaliseExpression;
import com.example.calculatorService.service.Tools.PrepareExpression;

import java.util.*;

public interface ReferenceService {
    /**
     * Найти ссылки на другие функции.
     * Синтаксис1: ref(номер_id);
     * Синтаксис2.1: ref(с_id..по_id);
     * Синтаксис2.2: ref(с_id..по_id, операция_объединения) операция_объединения может быть к примеру умножением (*), по умолчанию сложение (2.1);
     * @param expression
     * @return
     */
    default List<String> findFuncVarReferencesById(List<String> expression, FuncVarRepository funcRepo) throws NoSuchElementException, ReferenceResultIsEmpty{
        if(expression == null){
            return null;
        }
        //вставка результата другого выражения по id
        for (int i = 0; i < expression.size(); i++) {
            if(expression.get(i).equals("ref")){
                try {
                    String ids = expression.get(i + 2).replaceAll(" ", "");
                    //Когда мы хотим получить несколько результатов по id и както их объединить
                    if(ids.indexOf("..") > 0) {
                        //Операция объединения результатов выражений по умолчанию сложение
                        String operationBetweenResultsById = "+";
                        byte sizeLabelRef = 4; //Размер самой ссылки в выражении, для того, чтобы мы могли ее стереть после вставки значения по ней
                        //Ищем указана ли операция объединения результатов выражений
                        if(expression.get(i + 3).equals(",")){
                            operationBetweenResultsById = expression.get(i + 4);
                            sizeLabelRef = 6;
                        }

                        //Теперь разделяем диапазон иднексов
                        String[] idsArr = ids.split("\\.\\.");
                        try {
                            long start = Long.parseLong(idsArr[0]);
                            long end = Long.parseLong(idsArr[1]);
                            List<String> result = new ArrayList<>();
                            for (long j = start; j < end + 1; j++) {
                                FuncVar tempFunc = funcRepo.findById(j).get();
                                result.add("(");
                                result.addAll(tempFunc.getResult());
                                result.add(")");
                                result.add(operationBetweenResultsById);
                            }

                            result.remove(result.size() - 1);
                            expression.addAll(i, result);
                            for (byte j = 0; j < sizeLabelRef; j++) {
                                expression.remove(i + result.size());
                            }
                            i += result.size();
                        } catch (ArrayIndexOutOfBoundsException e){
                            e.printStackTrace();
                        }
                    } else {
                        long idFunc = Long.parseLong(ids);

                        FuncVar temp = funcRepo.findById(idFunc).get();
                        List<String> resultById = temp.getResult();
                        if(resultById != null){
                            expression.remove(i + 2);
                            expression.remove(i);
                            expression.addAll(i + 1, resultById);

                            i += resultById.size();
                        }
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();

                    return null;
                } catch (NullPointerException e){
                    throw new ReferenceResultIsEmpty();
                }
            }
        }

        return expression;
    }

    /**
     * Найти ссылки на другие функции по имени.
     * Синтаксис1: name(имя). Если только одна переменная имеет такое имя, то вернется ее значение.
     * Если несколько то вернеться их объединение.
     * Синтаксис2: name(имя,операция_объединения) операция_объединения может быть к примеру умножением (*), по умолчанию сложение (1);
     * @param expression
     * @return
     */
    default List<String> findFuncVarReferencesByName(List<String> expression, FuncVarRepository funcRepo) throws NoSuchElementException, ReferenceResultIsEmpty{
        if(expression == null){
            return null;
        }

        //вставка результата другого выражения по имени
        for (int i = 0; i < expression.size(); i++) {
            if(expression.get(i).equals("name")){
                try{
                    //Операция объединения результатов выражений по умолчанию сложение
                    String operationBetweenResultsById = "+";
                    byte sizeLabelRef = 4; //Размер самой ссылки в выражении, для того, чтобы мы могли ее стереть после вставки значения по ней
                    if(expression.get(i + 3).equals(",")){
                        operationBetweenResultsById = expression.get(i + 4);
                        sizeLabelRef = 6;
                    }

                    String name = expression.get(i + 2).replaceAll(" ", "");

                    List<FuncVar> temp = funcRepo.findByName(name);
                    //Если ничего не было найдено
                    if(temp.size() < 1){
                        throw new NoSuchElementException();
                    }

                    List<String> result = new ArrayList<>();
                    for (FuncVar t : temp) {
                        result.add("(");
                        result.addAll(t.getResult());
                        result.add(")");
                        result.add(operationBetweenResultsById);
                    }

                    result.remove(result.size() - 1);
                    expression.addAll(i, result);
                    for (byte j = 0; j < sizeLabelRef; j++) {
                        expression.remove(i + result.size());
                    }

                    i += result.size();
                } catch (NullPointerException e){
                    throw new ReferenceResultIsEmpty();
                }
            }
        }

        return expression;
    }

    /**
     * Найти ссылки на другие таблицы по имени.
     * Синтаксис: tname(имя, номер_строки). Указывается номер строки из котой нужно взять значение.
     * @param expression
     * @return
     */
    default List<String> findRangeTableReferencesByName(List<String> expression, RangeTableRepository tableRepo) throws NoSuchElementException, ReferenceResultIsEmpty {
        if(expression == null){
            return null;
        }

        //вставка результата другого выражения по имени
        for (int i = 0; i < expression.size(); i++) {
            if(expression.get(i).equals("tname")){
                try {
                    String name = expression.get(i + 2).replaceAll(" ", "");

                    List<RangeTable> temp = tableRepo.findByName(name);
                    int stringNumber = 0;
                    if(expression.get(i + 3).equals(",")){
                        stringNumber = Integer.parseInt(expression.get(i + 4)) - 1;
                        stringNumber = stringNumber < 0 ? 0 : stringNumber;
                    }
                    //Если ничего не было найдено
                    if(temp.size() < 1){
                        throw new NoSuchElementException();
                    }
                    List<String> result = temp.get(0).getResults().get(stringNumber).getResult();

                    expression.addAll(i, result);
                    for (byte j = 0; j < 6; j++) {
                        expression.remove(i + result.size());
                    }

                    i += result.size();
                } catch (IndexOutOfBoundsException | NumberFormatException e){
                    throw new TableReferenceErrorException();
                } catch (NullPointerException e){
                    throw new ReferenceResultIsEmpty();
                }
            }
        }

        return expression;
    }

    /**
     * Найти ссылки на другие таблицы по id.
     * Синтаксис: tref(id, номер_строки). Указывается номер строки из котой нужно взять значение.
     * @param expression
     * @return
     */
    default List<String> findRangeTableReferencesById(List<String> expression, RangeTableRepository tableRepo) throws NoSuchElementException, ReferenceResultIsEmpty, TableReferenceErrorException{
        if(expression == null){
            return null;
        }

        //вставка результата другого выражения по имени
        for (int i = 0; i < expression.size(); i++) {
            if(expression.get(i).equals("tref")){
                try {
                    int stringNumber = 0;
                    if(expression.get(i + 3).equals(",")){
                        //Получим номер строки
                        stringNumber = Integer.parseInt(expression.get(i + 4)) - 1;
                        stringNumber = stringNumber < 0 ? 0 : stringNumber;
                    }

                    //Получим id таблицы
                    long id = Long.parseLong(expression.get(i + 2).replaceAll(" ", ""));

                    RangeTable temp = tableRepo.findById(id).get();
                    List<String> result = temp.getResults().get(stringNumber).getResult();

                    expression.addAll(i, result);
                    for (byte j = 0; j < 6; j++) {
                        expression.remove(i + result.size());
                    }

                    i += result.size();
                } catch (IndexOutOfBoundsException | NumberFormatException e){
                    throw new TableReferenceErrorException();
                } catch (NullPointerException e){
                    throw new ReferenceResultIsEmpty();
                }
            }
        }

        return expression;
    }

    /**
     * Расчитать значение по выражению из таблицы с параметрами
     * Синтаксис: tcalc(name/id/expression, params). Указваеться либо таблицы из которой возьмется выражение, либо само выражение. И значения его параметров.
     * params указваються как: x=2&y=4
     * @param expression
     * @return
     */
    default List<String> calculateRangeTableReferences(List<String> expression, RangeTableRepository tableRepo, AnaliseExpression analiser) throws NoSuchElementException, ReferenceResultIsEmpty, TableReferenceErrorException{
        if(expression == null){
            return null;
        }
        //вставка результата другого выражения по имени
        for (int i = 0; i < expression.size(); i++) {
            if(expression.get(i).equals("tcalc")) {
                try {
                    HashMap<String, String> params = new HashMap<>();
                    int indexOfStartParams = expression.indexOf(",");
                    if (indexOfStartParams > i + 2) {
                        params = compareArgs(expression, indexOfStartParams + 1, ")");
                    }

                    List<String> possibleExpression = new ArrayList<>();
                    int k = i + 2;
                    while (!expression.get(k).equals(",")) {
                        possibleExpression.add(expression.get(k));
                        k++;
                    }

                    List<String> result;
                    //Если подозреваеться, что выражение на саммом деле ссылка
                    if (possibleExpression.size() == 1) {
                        RangeTable temp = null;
                        try {
                            temp = tableRepo.findByName(possibleExpression.get(0).replaceAll(" ", "")).get(0);
                        } catch (IndexOutOfBoundsException e1) {
                            try {
                                long id = Long.parseLong(possibleExpression.get(0).replaceAll(" ", ""));
                                temp = tableRepo.findById(id).get();
                            } catch (NumberFormatException e2) {
                            }
                        }

                        //Если смогли найти таблицу, то получаем ее выражение
                        if (temp != null) {
                            result = PrepareExpression.decompose(temp.getExpression());
                        }
                        //Если нет, то подозреваем, что это было выражение с одним элементом
                        else {
                            result = possibleExpression;
                        }
                    } else {
                        result = possibleExpression;
                    }

                    //Подставляем значение переменных в выражение
                    for (Map.Entry<String, String> p : params.entrySet()) {
                        for (int j = 0; j < result.size(); j++) {
                            if (p.getKey().equals(result.get(j))) {
                                result.set(j, p.getValue());
                            }
                        }
                    }
                    //Анализируем подготовленное выражение
                    result = analiser.analise(result);

                    //Удаляем функцию в две стадии
                    int j = i;
                    //Сначала удалим само выражение
                    while (!expression.get(j).equals(",")){
                        expression.remove(i);
                    }
                    expression.remove(i);
                    //Потом удалим переменые
                    while (!expression.get(j).equals(")")){
                        expression.remove(i);
                    }
                    expression.remove(i);

                    expression.addAll(i, result);

                    i += result.size();
                } catch (NullPointerException e){
                    throw new ReferenceResultIsEmpty();
                }
            }
        }

        return expression;
    }

    private HashMap<String, String> compareArgs(List<String> expression, int startIndex, String endValue) throws IndexOutOfBoundsException{
        HashMap<String, String> params = new HashMap<>();

        int i = startIndex;
        while (!expression.get(i).equals(endValue)){
            if(expression.get(i).equals("=")){
                params.put(expression.get(i-1), expression.get(i+1));
            }
            i++;
        }

        return params;
    }

    default List<String> findNCalculateCustomFunc(List<String> expression, CustomFunctionRepository customRepo, FuncVarRepository funcRepo, RangeTableRepository tableRepo, AnaliseExpression analiser) {
        try {
            List<CustomFunction> funcs = customRepo.findAll();

            for (CustomFunction c : funcs) {
                for (int i = 0; i < expression.size(); i++) {
                    if (expression.get(i).equals(c.getName())){
                        switch (c.getTypeSearch()){
                            case TWO_SIDES:
                                ModelCustomTwoSides model1 = new ModelCustomTwoSides(c, funcRepo, tableRepo);
                                i = model1.operation(expression, i, analiser);
                                break;
                            case RIGHT_SIDE:
                                ModelCustomRightSide model2 = new ModelCustomRightSide(c);
                                i = model2.operation(expression, i, analiser);
                                break;
                        }
                    }
                }
            }

            return expression;
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }
}
