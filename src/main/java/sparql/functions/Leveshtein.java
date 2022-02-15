package sparql.functions;

import java.util.List;

import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.NodeValueString;
import org.apache.jena.sparql.function.FunctionBase;
import org.apache.jena.sparql.function.FunctionBase2;

public class Leveshtein extends FunctionBase2 {
	
	public Leveshtein() {
		super();
	}
	
	@Override
	public NodeValue exec(NodeValue v1, NodeValue v2) {
		NodeValue v = new NodeValueString("testOk");
		System.out.println("HERE!!!");
		return v;
	}


}
