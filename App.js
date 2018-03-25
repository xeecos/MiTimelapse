/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, { Component } from 'react';
import {
  Platform,
  StyleSheet,
  Text,
  Button, TextInput,
  View, NativeModules, AppState
} from 'react-native';


export default class App extends Component {
  constructor() {
    super();
    this.state = {
      interval: 1,
      count: 100,
      rest: 100,
      capturing: false,
      appState: AppState.currentState
    }
  }
  componentDidMount() {

    AppState.addEventListener('change', this._handleAppStateChange);
  }

  componentWillUnmount() {
    AppState.removeEventListener('change', this._handleAppStateChange);
  }
  _handleAppStateChange = (nextAppState) => {
    if (this.state.appState.match(/inactive|background/) && nextAppState === 'active') {
      console.log('App has come to the foreground!');
      if (this.state.capturing) {
        this.continueCapture();
      }
    } else {

    }
    this.state.appState = nextAppState;
  }
  continueCapture() {
    if (this.state.rest > 0) {
      setTimeout((e => {
        if (this.state.capturing) {
          var RNMiService = NativeModules.RNMiService;
          RNMiService.capture();
        }
      }).bind(this), this.state.interval * 1000);
      this.setState({ rest: this.state.rest - 1 });
    } else {
      this.setState({ rest: 0, capturing: false });
    }
  }
  render() {
    return (
      <View style={styles.container}>

        <Button onPress={() => {
          var RNMiService = NativeModules.RNMiService;
          RNMiService.openService();
        }} title="Open Service"
          color="#841584"
          accessibilityLabel="Open Service" />
        <TextInput
          style={{ width: 100, height: 40, borderColor: 'gray', borderWidth: 1 }}
          onChangeText={(text) => this.setState({ interval: text * 1 })}
        >{this.state.interval}</TextInput>
        <TextInput
          style={{ width: 100, height: 40, borderColor: 'gray', borderWidth: 1 }}
          onChangeText={(text) => this.setState({ count: text, rest: text })}
        >{this.state.count}</TextInput>
        <Button onPress={((e) => {
          if (!this.state.capturing) {
            this.state.rest = this.state.count;
            this.state.capturing = true;
            this.continueCapture();
          } else {
            this.setState({ capturing: false });
          }
        }).bind(this)} title={!this.state.capturing ? "开始拍摄" : "暂停拍摄"}
          color="#841584"
          accessibilityLabel={!this.state.capturing ? "开始拍摄" : "暂停拍摄"} />
        <Button onPress={() => {

        }} title="合成视频"
          color="#841584"
          accessibilityLabel="合成视频" />
        <Text >
          {'rest:' + this.state.rest}
        </Text>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
});