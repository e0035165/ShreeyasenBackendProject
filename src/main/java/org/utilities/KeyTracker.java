package org.utilities;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;



public class KeyTracker {
    public static List<String> getAllKeys(String testValue) {
        List<String>allStrings = List.of("Software engineering",
             "Engineering",
             "Java",
             "Javascript",
             "Html",
             "Lifecycle",
             "Spring",
             "Spring Boot", "Spring framework", "ReactJS Framework","Software developer",
                "Software Tester","Hardware","Hardwired","wired","Software","Python","Anaconda","Pytorch","Software Development LifeCycle","developer",
                "Selenium","Appium","Playwright","RESTapi","REST","Docker","Microservices","services","TestNG","JUnit","API","Spring Boot Container"
        );
        List<String>filteredStrings = allStrings.stream().map(KeyTracker::processString).toList();
        Map<Integer,Set<Node>>LevelHeader = new HashMap<>();
        Set<Node>allSet = LevelHeader.getOrDefault(0,new HashSet<>());
        allSet.add(Node.initialize());
        LevelHeader.put(0,allSet);
        filteredStrings.stream().forEach(x->{
            Node startNode = Node.initialize();
            for(int i=0;i<x.length()-1;++i) {
                Set<Node>levelVariable = LevelHeader.getOrDefault(i+1,new HashSet<>());
                startNode = startNode.setNextChild(x.charAt(i),false,null);
                levelVariable.add(startNode);
                LevelHeader.put(i+1,levelVariable);

            }
            Set<Node>levelVariable = LevelHeader.getOrDefault(x.length(),new HashSet<>());
            startNode=startNode.setNextChild(x.charAt(x.length()-1),true,x.length());
            levelVariable.add(startNode);
            LevelHeader.put(x.length(),levelVariable);
        });
        Integer levelCount = 1;
        while(LevelHeader.containsKey(levelCount)) {
            Set<Node>getNodes = LevelHeader.get(levelCount);
            for(Node node:getNodes) {
                Node.setFailure(levelCount,node);
            }
            levelCount++;
        }


        //String testValue = "I am a Hardware and Software developer.I have Hardwired to expertise in Spring framework. I have been a software tester for six months." +
        //        "Then I have six months experience in Microservices and I have completed testing using Spring Boot Container.";

        System.out.println(Node.getAllValues(processString(testValue)));
        System.out.println(Node.getAllkeywords(processString(testValue)));
        return Node.getAllkeywords(processString(testValue));

    }

    public static String processString(String value) {
        return value.replaceAll(" ","")
                .replaceAll("\\.","")
                .toLowerCase();
    }


    private static class Node {

        public static Node startNode=null;
        public Node parent;
        public Node failure;
        public Character S;
        public Node[] nextChildren = new Node[26];
        public Boolean end = false;
        public int endCount = 0;
        public List<Integer> endLength = new ArrayList<>();
        public static ArrayDeque<Node>allNodes = new ArrayDeque<>();


        private Node() {

        }

        public String toString() {
            return "Character: "+this.S+" WITH PARENT: "+this.parent.S+" WITH FAILURE: "+this.failure.S+" end="+this.end;
        }


        public static void setFailure(Integer level, Node node) {
            if(level==1) {
                node.failure=node.parent;
                node.endCount+=node.failure.endCount;
                return;
            } else {
                Node failure = node.parent.failure;
                while(failure.nextChildren[node.S-'a']==null && failure!=startNode) {
                    failure=failure.failure;
                }
                if(failure.nextChildren[node.S-'a']==null) {
                    node.failure=failure;
                } else {
                    node.failure=failure.nextChildren[node.S-'a'];
                }
                node.endCount+=node.failure.endCount;
                node.endLength.addAll(node.failure.endLength);
                return;
            }
        }


        public static Integer getAllValues(String actualValue) {
            Node startNode = initialize();
            Integer count = 0;
            for(int i=0;i<actualValue.length();++i) {
                Character S =actualValue.charAt(i);
                startNode = startNode.getNextChild(startNode,S);
                count+=startNode.endCount;
            }
            return count;
        }

        public static List<String>getAllkeywords(String actualValue) {
            List<String>allValues=new ArrayList<>();
            Node startNode = initialize();
            for(int i=0;i<actualValue.length();++i) {
                Character S =actualValue.charAt(i);
                startNode = startNode.getNextChild(startNode,S);
                if(startNode.endCount>0) {
                    allValues.addAll(getAllStrings(startNode.endLength,actualValue,i));
                }

            }
            return allValues;
        }

        public static List<String> getAllStrings(List<Integer> rangeValues, String bat, Integer currentIndex) {
            List<String>allValues = new ArrayList<>();
            allValues=rangeValues.stream().map(x->bat.substring(currentIndex+1-x,currentIndex+1)).toList();
            return allValues;
        }

        public static Node initialize() {
            if(startNode==null) {
                startNode=new Node();
                startNode.S = '$';
                startNode.parent=startNode;
                startNode.failure=startNode;
                Arrays.stream(startNode.nextChildren).forEach(x->x=null);
                return startNode;
            } else {
                //log.log(Level.INFO,"Starting node '$' already initialized");
                return startNode;
            }

        }

        public Node(Node parent, char S) {
            this.parent=parent;
            this.S=S;
            parent.nextChildren[S-'a']=this;
            Arrays.stream(this.nextChildren).forEach(x->x=null);
        }



        public Node getNextChild(Node A, Character S) {
            Node expectedChild = A.nextChildren[S-'a'];
            Node temp = A.failure;
            while(expectedChild==null) {
                expectedChild=temp.nextChildren[S-'a'];
                if(temp==startNode) {
                    expectedChild = expectedChild==null ? temp:expectedChild;
                    break;
                } else {
                    temp=temp.failure;
                }
            }

            return expectedChild;
        }



        public Node setNextChild(Character S, Boolean endOfString,Object obj) {
            if(this.nextChildren[S-'a']==null) {
                Node child = new Node(this,S);
                int idx = S-'a';
                this.nextChildren[idx]=child;
                child.end= (endOfString||child.end);
                child.endCount=child.end ? 1:0;
                if(obj!=null){
                    child.endLength.add((Integer) obj);
                }
                return child;
            } else {
                this.nextChildren[S-'a'].end=(endOfString || this.nextChildren[S-'a'].end);
                if(endOfString) {
                    this.nextChildren[S-'a'].endCount+=1;
                    this.nextChildren[S-'a'].endLength.add((Integer) obj);
                }

                return this.nextChildren[S-'a'];
            }

        }


    }
}