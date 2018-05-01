import java.util.ArrayList;

public class RouteTrie {
    RouteTrieNode rootNode;

    public RouteTrie(){
        rootNode = new RouteTrieNode("Root");
    }

    public void addRoute(Route route){
        //a/b/c  a b c
        RouteTrieNode nodeNow = rootNode;
        String path = clearPath(route.path);


        String []pathData = path.split("/");

        for (int i=0;i<pathData.length;i++){
            if(!nodeNow.containsChildNode(pathData[i])){
                RouteTrieNode newNode = new RouteTrieNode(pathData[i]);
                nodeNow.addChildNode(newNode);
            }
            nodeNow = nodeNow.getChildNode(pathData[i]);
            if(i==pathData.length-1){
                nodeNow.routes.add(route);
            }
        }


    }

    public Route getRoute(String path, Method method){
        RouteTrieNode nodeNow = rootNode;
        path = clearPath(path);
        String []pathData = path.split("/");
        for (int i=0;i<pathData.length;i++){
            if(nodeNow.containsChildNode(pathData[i])) {
                nodeNow = nodeNow.getChildNode(pathData[i]);
                continue;
            }
            return null;
        }
        for(Route r : nodeNow.routes){
            if(r.method == method){
                return r;
            }
        }
        return null;

    }

    public boolean containsRoute(Route route){
        RouteTrieNode nodeNow = rootNode;
        String path = clearPath(route.path);


        String []pathData = path.split("/");
        for (int i=0;i<pathData.length;i++){
            if(nodeNow.containsChildNode(pathData[i])) {
                nodeNow = nodeNow.getChildNode(pathData[i]);
                continue;
            }
            return false;
        }
        for(Route r : nodeNow.routes){
            if(r.method == route.method){
                return true;
            }
        }
        return  false;
    }

    public String clearPath(String path){
        if(path.startsWith("/")){
            path = path.substring(1);
        }
        if(path.endsWith("/")){
            path = path.substring(0,path.length()-1);
        }
        return path;
    }

//    public String func(RouteTrieNode node){
//        if(node.childNodes.size()==0){
//            return node.path+"\n";
//        }else{
//            String str = node.path;
//            for(RouteTrieNode temp:node.childNodes){
//                str+="--";
//                str+=func(temp);
//            }
//            return str;
//        }
//    }
//
//    public String toString(){
//        String str = func(rootNode);
//        return str;
//    }
}

class RouteTrieNode{
    String path;
    ArrayList<Route> routes;
    ArrayList<RouteTrieNode> childNodes;

    RouteTrieNode(String path){
        this.path = path;
        routes = new ArrayList<Route>();
        childNodes = new ArrayList<RouteTrieNode>();
    }

    public void AddNode(String path){
        RouteTrieNode newNode = new RouteTrieNode(path);
        this.childNodes.add(newNode);
    }

    public boolean containsChildNode(String path){
        for (RouteTrieNode node:childNodes){
            if(node.path.equals(path)){
                return true;
            }
        }
        return false;
    }

    public RouteTrieNode getChildNode(String path){
        for (RouteTrieNode node:childNodes){
            if(node.path.equals(path)){
                return node;
            }
        }
        return null;
    }

    public void addChildNode(RouteTrieNode node){
        this.childNodes.add(node);
    }


}

class Route{
    String path;
    Method method;
    Route(String path, Method method){
        this.path = path;
        this.method = method;
    }
}

enum Method{
    POST,
    GET,
    PUT
}
