import React from 'react'
import './visualization.css'

class QueryPlan extends React.Component {

  planInit (el) {
    if (el != null && !this.plan) {
      const NeoConstructor = neo.queryPlan
      this.plan = NeoConstructor(el)
      this.plan.display(this.props.plan)
    }
  }

  render () {
    return (
      <svg className='neod3plan' ref={this.planInit.bind(this)} />
    )
  }
}

QueryPlan.propTypes = {
  plan: React.PropTypes.object.isRequired
}
export default QueryPlan
