import React from 'react'
import { connect } from 'react-redux'
import * as actions from '../actions'
import ListItem from 'grommet/components/List'
import FormField from 'grommet/components/FormField'
import {Drawer, DrawerBody, DrawerHeader} from 'nbnmui/drawer'

import styles from './style.css'

const visualSettings =
  [
    {
      cmdchar: {
        displayName: 'Command char',
        tooltip: 'Something'
      }
    },
    {
      maxHistory: {
        displayName: 'Max stream history',
        tooltip: 'Max number of history entries. When reached, old entries gets retired.'
      }
    }
  ]

export const SettingsComponent = ({settings, onSettingsSave = () => {}}) => {
  const mappedSettings = visualSettings.map((visualSetting) => {
    const setting = Object.keys(visualSetting)[0]
    const visual = visualSetting[setting].displayName
    const tooltip = visualSetting[setting].tooltip
    return (
      <ListItem>
        <FormField label={visual} className={'setting ' + styles.setting}>
          <input onChange={(event) => {
            settings[setting] = event.target.value
            onSettingsSave(settings)
          }} defaultValue={settings[setting]} suggestions={[tooltip]} />
        </FormField>
      </ListItem>
    )
  })

  return (
    <Drawer id='db-settings'>
      <DrawerHeader title='Browser Settings' />
      <DrawerBody>
        {mappedSettings}
      </DrawerBody>
    </Drawer>
  )
}

const mapStateToProps = (state) => {
  return {
    settings: state.settings
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    onSettingsSave: (settings) => {
      dispatch(actions.update(settings))
    }
  }
}

const Settings = connect(mapStateToProps, mapDispatchToProps)(SettingsComponent)
export default Settings
