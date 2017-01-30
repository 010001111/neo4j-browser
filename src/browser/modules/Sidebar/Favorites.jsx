import React from 'react'
import { connect } from 'react-redux'
import { withBus } from 'react-suber'
import * as editor from 'shared/modules/editor/editorDuck'
import * as favorite from 'shared/modules/favorites/favoritesDuck'
import Favorite from './Favorite'
import {Drawer, DrawerBody, DrawerHeader} from 'nbnmui/drawer'

export const Favorites = (props) => {
  const ListOfFavorites = props.scripts.map((entry) => {
    return <Favorite key={entry.id} name={entry.name} content={entry.content} onItemClick={props.onItemClick} />
  })
  return (
    <Drawer id='db-favorites'>
      <DrawerHeader title='Favorites' />
      <DrawerBody>
        {ListOfFavorites}
      </DrawerBody>
    </Drawer>
  )
}

const mapStateToProps = (state) => {
  return {
    scripts: state.favorites.scripts || []
  }
}
const mapDispatchToProps = (dispatch, ownProps = {}) => {
  return {
    onItemClick: (cmd) => {
      ownProps.bus.send(editor.SET_CONTENT, editor.setContent(cmd))
    },
    removeClick: (id) => {
      dispatch(favorite.removeFavorite(id))
    }
  }
}

export default withBus(connect(mapStateToProps, mapDispatchToProps)(Favorites))
