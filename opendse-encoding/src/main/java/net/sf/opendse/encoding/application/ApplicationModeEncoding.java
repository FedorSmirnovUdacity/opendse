package net.sf.opendse.encoding.application;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.opt4j.satdecoding.Constraint;

import com.google.inject.Inject;

import net.sf.opendse.encoding.ApplicationEncoding;
import net.sf.opendse.encoding.variables.ApplicationVariable;
import net.sf.opendse.encoding.variables.Variables;
import net.sf.opendse.model.Application;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Task;
import net.sf.opendse.model.properties.DependencyPropertyService;
import net.sf.opendse.model.properties.TaskPropertyService;

/**
 * The {@link ApplicationModeEncoding} is an {@link ApplicationEncoding} that
 * enables the usage of different {@link ApplicationModeConstraintGenerator} to
 * process different parts of the application.
 * 
 * @author Fedor Smirnov
 *
 */
public class ApplicationModeEncoding implements ApplicationEncoding {

	protected final ApplicationConstraintGeneratorManager generatorManager;

	@Inject
	public ApplicationModeEncoding(ApplicationConstraintGeneratorManager generatorManager) {
		this.generatorManager = generatorManager;
	}

	@Override
	public Set<Constraint> toConstraints(Application<Task, Dependency> application) {
		Set<Constraint> applicationConstraints = new HashSet<Constraint>();
		Map<String, Set<ApplicationVariable>> applicationModeMap = filterApplicationModes(application);
		// generate the constraints for each mode
		for (Entry<String, Set<ApplicationVariable>> entry : applicationModeMap.entrySet()) {
			String modeString = entry.getKey();
			Set<ApplicationVariable> variables = entry.getValue();
			ApplicationModeConstraintGenerator constraintGenerator = generatorManager
					.getConstraintGenerator(modeString);
			applicationConstraints.addAll(constraintGenerator.toConstraints(variables));
		}
		return applicationConstraints;
	}

	/**
	 * Filters the tasks and dependencies according to their activation modes.
	 * 
	 * @param application
	 *            the application graph
	 * @return map where the activation modes are mapped onto the sets of their
	 *         application variables
	 */
	protected Map<String, Set<ApplicationVariable>> filterApplicationModes(Application<Task, Dependency> application) {
		Map<String, Set<ApplicationVariable>> result = new HashMap<String, Set<ApplicationVariable>>();
		// process the tasks
		for (Task task : application) {
			String modeString = TaskPropertyService.getActivationMode(task).getXmlName();
			if (!result.containsKey(modeString)) {
				result.put(modeString, new HashSet<ApplicationVariable>());
			}
			result.get(modeString).add(Variables.var(task));
		}
		// process the dependencies
		for (Dependency dependency : application.getEdges()) {
			String modeString = DependencyPropertyService.getActivationMode(dependency).getXmlName();
			if (!result.containsKey(modeString)) {
				result.put(modeString, new HashSet<ApplicationVariable>());
			}
			Task source = application.getSource(dependency);
			Task destination = application.getDest(dependency);
			result.get(modeString).add(Variables.var(dependency, source, destination));
		}
		return result;
	}
}