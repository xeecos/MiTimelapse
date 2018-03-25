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
  View, NativeModules, AppState, DeviceEventEmitter
} from 'react-native';


export default class App extends Component {
  constructor() {
    super();
    this.state = {
      interval: 1,
      count: 100,
      rest: 100,
      capturing: false,
      msg: 0,
      appState: AppState.currentState
    }
  }
  componentDidMount() {
    var RNMiService = NativeModules.RNMiService;
    RNMiService.requestStorage();
    AppState.addEventListener('change', this._handleAppStateChange.bind(this));
    DeviceEventEmitter.addListener('fromDevice', this._fromDevice.bind(this));
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
  _fromDevice(str) {
    this.setState({ msg: str });
  }
  continueCapture() {
    if (this.state.rest > 0) {
      setTimeout((e => {
        if (this.state.capturing) {
          var RNMiService = NativeModules.RNMiService;
          RNMiService.capture();
        }
      }).bind(this), this.state.interval * 1000);
      this.setState({ msg: 0, rest: this.state.rest - 1 });
    } else {
      this.setState({ rest: 0, capturing: false });
    }
  }
  render() {
    return (
      <View style={styles.container}>
        <View style={styles.service}>
          <Button onPress={() => {
            var RNMiService = NativeModules.RNMiService;
            RNMiService.openService();
          }} title="打开辅助设置"
            color="#841584"
            accessibilityLabel="打开辅助设置" />
        </View>
        <Text >
          {"间隔:"}
        </Text>
        <TextInput
          style={{ width: 100, height: 40, borderColor: 'gray', borderWidth: 1 }}
          onChangeText={(text) => this.setState({ interval: text * 1 })}
        >{this.state.interval}</TextInput>
        <Text >
          {"拍摄数量:"}
        </Text>
        <TextInput
          style={{ width: 100, height: 40, borderColor: 'gray', borderWidth: 1 }}
          onChangeText={(text) => this.setState({ count: text * 1, rest: text * 1 })}
        >{this.state.count}</TextInput>
        <View style={styles.capture}>
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
        </View>
        <View style={styles.combine}>
          <Button onPress={(() => {
            var RNMiService = NativeModules.RNMiService;
            RNMiService.combineMovie(this.state.count);
          }).bind(this)} title="合成最新视频"
            color="#841584"
            accessibilityLabel="合成最新视频" />
        </View>
        <Text >
          {this.state.msg ? this.state.msg : ('剩余:' + this.state.rest)}
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
  service: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
    position: 'absolute',
    top: 10,
    left: 10
  },
  capture: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
    position: 'absolute',
    top: 10,
    right: 10
  },
  combine: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
    position: 'absolute',
    bottom: 10,
    right: 10
  },
});
