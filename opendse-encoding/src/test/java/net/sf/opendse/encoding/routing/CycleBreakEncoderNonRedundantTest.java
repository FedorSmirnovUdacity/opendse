package net.sf.opendse.encoding.routing;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.opt4j.satdecoding.Constraint;

import edu.uci.ics.jung.graph.util.EdgeType;
import net.sf.opendse.encoding.variables.CLRR;
import net.sf.opendse.encoding.variables.T;
import net.sf.opendse.encoding.variables.Variables;
import net.sf.opendse.model.Architecture;
import net.sf.opendse.model.Communication;
import net.sf.opendse.model.Link;
import net.sf.opendse.model.Resource;
import verification.ConstraintVerifier;

public class CycleBreakEncoderNonRedundantTest {
	@Test
	public void test() {
		Resource r0 = new Resource("r0");
		Resource r1 = new Resource("r1");
		Resource r2 = new Resource("r2");
		Link l0 = new Link("l0");
		Link l1 = new Link("l1");
		Architecture<Resource, Link> routing = new Architecture<Resource, Link>();
		routing.addEdge(l0, r0, r1, EdgeType.UNDIRECTED);
		routing.addEdge(l1, r2, r1, EdgeType.UNDIRECTED);
		Communication comm = new Communication("comm");
		T commVar = Variables.varT(comm);
		CLRR inLink1 = Variables.varCLRR(comm, l0, r0, r1);
		CLRR inLink2 = Variables.varCLRR(comm, l1, r2, r1);
		Set<Object> activated = new HashSet<Object>();
		activated.add(inLink1);
		CycleBreakEncoderNonRedundant encoder = new CycleBreakEncoderNonRedundant();
		Set<Constraint> cs = encoder.toConstraints(commVar, routing);
		assertEquals(3, cs.size());
		ConstraintVerifier verifyOneInLinkOnly = new ConstraintVerifier(activated, new HashSet<Object>(), cs);
		verifyOneInLinkOnly.verifyVariableDeactivated(inLink2);
	}
}