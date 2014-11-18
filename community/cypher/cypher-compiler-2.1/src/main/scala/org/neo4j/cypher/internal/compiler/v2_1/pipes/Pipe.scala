/**
 * Copyright (c) 2002-2014 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.cypher.internal.compiler.v2_1.pipes

import org.neo4j.cypher.internal.compiler.v2_1._
import org.neo4j.cypher.internal.compiler.v2_1.executionplan.Effects
import org.neo4j.cypher.internal.compiler.v2_1.mutation.Effectful
import org.neo4j.cypher.internal.compiler.v2_1.planDescription.{NullPlanDescription, PlanDescription}
import org.neo4j.cypher.internal.compiler.v2_1.symbols._
import org.neo4j.helpers.ThisShouldNotHappenError

trait PipeMonitor {
  def startSetup(queryId: AnyRef, pipe: Pipe)
  def stopSetup(queryId: AnyRef, pipe: Pipe)
  def startStep(queryId: AnyRef, pipe: Pipe)
  def stopStep(queryId: AnyRef, pipe: Pipe)
}

/**
 * Pipe is a central part of Cypher. Most pipes are decorators - they
 * wrap another pipe. ParamPipe and NullPipe the only exception to this.
 * Pipes are combined to form an execution plan, and when iterated over,
 * the execute the query.
 */
trait Pipe extends Effectful {
  self: Pipe =>

  def monitor: PipeMonitor

  def dup(sources: List[Pipe]): Pipe

  def createResults(state: QueryState) : Iterator[ExecutionContext] = {
    val decoratedState = state.decorator.decorate(self, state)
    monitor.startSetup(state.queryId, self)
    val innerResult = internalCreateResults(decoratedState)
    val result = new Iterator[ExecutionContext] {
      def hasNext = innerResult.hasNext
      def next() = {
        monitor.startStep(state.queryId, self)
        val value = innerResult.next()
        monitor.stopStep(state.queryId, self)
        value
      }
    }
    monitor.stopSetup(state.queryId, self)
    state.decorator.decorate(self, result)
  }

  protected def internalCreateResults(state: QueryState): Iterator[ExecutionContext]

  def symbols: SymbolTable

  def planDescription: PlanDescription

  def sources: Seq[Pipe]

  def localEffects: Effects = Effects.ALL

  def effects: Effects = localEffects

  /*
  Runs the predicate on all the inner Pipe until no pipes are left, or one returns true.
   */
  def exists(pred: Pipe => Boolean): Boolean
}

case class NullPipe(symbols: SymbolTable = SymbolTable())
                   (implicit val monitor: PipeMonitor) extends Pipe {
  def internalCreateResults(state: QueryState) =
    Iterator(state.initialContext getOrElse ExecutionContext.empty)

  def exists(pred: Pipe => Boolean) = pred(this)

  def planDescription: PlanDescription = new NullPlanDescription(this)

  override def localEffects = Effects.NONE

  def dup(sources: List[Pipe]): Pipe = this

  def sources: Seq[Pipe] = Seq.empty
}

abstract class PipeWithSource(source: Pipe, val monitor: PipeMonitor) extends Pipe {
  override def createResults(state: QueryState): Iterator[ExecutionContext] = {
    val sourceResult = source.createResults(state)

    val decoratedState = state.decorator.decorate(this, state)
    val result = internalCreateResults(sourceResult, decoratedState)
    state.decorator.decorate(this, result)
  }

  protected def internalCreateResults(state: QueryState): Iterator[ExecutionContext] =
    throw new ThisShouldNotHappenError("Andres", "This method should never be called on PipeWithSource")

  protected def internalCreateResults(input:Iterator[ExecutionContext], state: QueryState): Iterator[ExecutionContext]

  override val sources: Seq[Pipe] = Seq(source)

  override def effects =
    sources.foldLeft(localEffects)(_ | _.effects)

  def exists(pred: Pipe => Boolean) = pred(this) || source.exists(pred)
}