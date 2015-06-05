package com.knowprocess.deployment;


public class ProcessDefiner {

    public org.activiti.bpmn.model.Process parse(String markup) {
        org.activiti.bpmn.model.Process process = new org.activiti.bpmn.model.Process();
        String[] lines = markup.split("\n");
        for (String line : lines) {
//            line = line.trim();
            if (line.length() == 0) {
                continue;
            }
            if (Character.isDigit(line.charAt(0))) {
                System.out.println("Adding serial task: " + line);
                // Pattern assignee = Pattern.compile("[+]\\w");
                // // Matcher matcher = Matcher;
                // Matcher matcher = assignee.matcher(line);
                // boolean found = matcher.find();
                // System.out.println(matcher.group());
//            }else if (isUnordered) { 
//                

            }
        }
        return process;
    }
}
