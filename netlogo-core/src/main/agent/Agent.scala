// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import
  org.nlogo.{ core, api },
    core.{ I18N, Program },
    api.{ AgentException, Agent => ApiAgent }

// NOTE: If we see performance problems, we may be able to optimize by making
// a java base class for this which contains fields for id, variables, world, etc.
// This has the potential to remove method-call overhead in java subclasses, like
// InstructionJ does for instructions.
abstract class Agent(val world: World) extends ApiAgent with Comparable[Agent] {
  private[agent] var _id = 0L
  def id = _id

  private[agent] var _variables: Array[AnyRef] = null
  def variables = _variables

  private[agent] def agentKey: AnyRef = Double.box(_id)

  override def compareTo(a: Agent): Int =
    id compareTo a.id

  @throws(classOf[AgentException])
  private[agent] def realloc(oldProgram: Program, newProgram: Program): Agent

  def getVariable(vn: Int): AnyRef

  /**
   * Returns the name of the variable with the given index. Works for built-in, *-own, and breed variables.
   * @param vn the index of the variable
   */
  def variableName(vn: Int): String

  @throws(classOf[AgentException])
  def setVariable(vn: Int, value: AnyRef)

  @throws(classOf[AgentException])
  def getTurtleVariable(vn: Int): AnyRef

  @throws(classOf[AgentException])
  def getBreedVariable(name: String): AnyRef

  @throws(classOf[AgentException])
  def getLinkBreedVariable(name: String): AnyRef

  @throws(classOf[AgentException])
  def getLinkVariable(vn: Int): AnyRef

  @throws(classOf[AgentException])
  def getPatchVariable(vn: Int): AnyRef

  @throws(classOf[AgentException])
  def getTurtleOrLinkVariable(varName: String): AnyRef

  @throws(classOf[AgentException])
  def setTurtleVariable(vn: Int, value: AnyRef)

  @throws(classOf[AgentException])
  def setTurtleVariable(vn: Int, value: Double)

  @throws(classOf[AgentException])
  def setLinkVariable(vn: Int, value: AnyRef)

  @throws(classOf[AgentException])
  def setLinkVariable(vn: Int, value: Double)

  @throws(classOf[AgentException])
  def setBreedVariable(name: String, value: AnyRef)

  @throws(classOf[AgentException])
  def setLinkBreedVariable(name: String, value: AnyRef)

  @throws(classOf[AgentException])
  def setPatchVariable(vn: Int, value: AnyRef)

  @throws(classOf[AgentException])
  def setPatchVariable(vn: Int, value: Double)

  @throws(classOf[AgentException])
  def setTurtleOrLinkVariable(varName: String, value: AnyRef)

  @throws(classOf[AgentException])
  def getPatchAtOffsets(dx: Double, dy: Double): Patch

  def classDisplayName: String

  def agentBit: Int

  @throws(classOf[AgentException])
  private[agent] def wrongTypeForVariable(name: String, expectedClass: Class[_], value: AnyRef) {
    throw new AgentException(I18N.errors.getN("org.nlogo.agent.Agent.wrongTypeOnSetError",
        classDisplayName, name, api.Dump.typeName(expectedClass), api.Dump.logoObject(value)))
  }
}
