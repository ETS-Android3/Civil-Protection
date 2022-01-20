var map;
var devices = {};
var rectangle = null;
document.addEventListener("DOMContentLoaded", function () {
	getDevices();
});

function initMap() {

	const lat = 37.968483044347;
	const lng = 23.766819125476;
	const MapOptions = {
		mapTypeId: 'roadmap',
		zoom: 18,
		center: { lat, lng }
	}
	const mapDiv = document.getElementById('map');
	map = new google.maps.Map(mapDiv, MapOptions);

}

function getDevices() {

	axios
		.get("http://localhost:8080/api/devices/")
		.then(function (response) {
			populateMarkers(response.data);
		})
		.catch(function (error) {
			console.error('Something went wrong!');
			console.error('Trying again in 10 seconds...');
			setTimeout(getDevices, 10 * 1000);
		})

}

function populateMarkers(devicesData) {

	devicesData.androidDevicesList.forEach(function (androidDevice) {
		addAndroidDeviceMarker(androidDevice);
	});

	devicesData.iotDevicesList.forEach(function (iotDevice) {
		addSensorMarker(iotDevice)
	});

	// Check if there is an event from 2 sensors at the same time
	if (devicesData.iotDevicesList.length === 2 &&
	    devicesData.iotDevicesList[0].dangerLevel !== 'NONE' &&
        devicesData.iotDevicesList[1].dangerLevel !== 'NONE') {
		drawRectangle(devicesData.iotDevicesList);
	} else {
		clearRectangle();
	}

	setTimeout(() => {
		getDevices();
	}, 1 * 1000);

}

function addSensorMarker(iotDevice) {

    // If device already exists, update marker position, icon and info window content
    if (devices[iotDevice.device_id]) {
		updateSensorDeviceMarker(iotDevice);
	} else {
		const marker = new google.maps.Marker({map: map});
		const infoWindow = new google.maps.InfoWindow({anchor: marker});
		const rangeMarker = new google.maps.Marker({
			optimized: false,
			map: map
		});

		devices[iotDevice.device_id] = {
			'device': iotDevice,
			'deviceType': 'sensor-device',
			'marker': marker,
			'iw': infoWindow,
			'range': rangeMarker,
		};

		rangeMarker.addListener("click", () => {
			toggleInfoWindow(iotDevice.device_id);
		});

		marker.addListener("click", () => {
			toggleInfoWindow(iotDevice.device_id);
		});

		updateSensorDeviceMarker(iotDevice);
	}

}

function addAndroidDeviceMarker(androidDevice) {

	// If device already exists, update marker position, icon and info window content
	if (devices[androidDevice.device_id]) {
		updateAndroidDeviceMarker(androidDevice)
	} else {
		const marker = new google.maps.Marker({map: map,});
		const infoWindow = new google.maps.InfoWindow({anchor: marker});

		devices[androidDevice.device_id] = {
			'device': androidDevice,
			'deviceType': 'android-device',
			'marker': marker,
			'iw': infoWindow,
		};

		marker.addListener("click", () => {
			toggleInfoWindow(androidDevice.device_id);
		});

		updateAndroidDeviceMarker(androidDevice);
	}

}

function updateSensorDeviceMarker(iotDevice) {

	let markerIconUrl = '/images/iot-device-icon.svg'
	if (iotDevice.dangerLevel == 'DANGER_LEVEL_MEDIUM') {
		markerIconUrl = '/images/iot-medium-danger-icon.svg';
	} else if (iotDevice.dangerLevel == 'DANGER_LEVEL_HIGH') {
		markerIconUrl = '/images/iot-high-danger-icon.svg';
	}

	devices[iotDevice.device_id].marker.setIcon({
		url: markerIconUrl,
		scaledSize: new google.maps.Size(40, 46),
		anchor: new google.maps.Point(20, 46),
	});

	const now = moment();
	const lastSeen = moment(iotDevice.lastUpdate * 1000);

	let rangeIcon = '/images/range-active.svg';
	// If IoT device didn't send sensor data or its session timed out
	if ((iotDevice.smoke == null && iotDevice.gas == null && iotDevice.temperature == null && iotDevice.uv == null) ||
	    (now.diff(lastSeen, 'seconds') > 30)) {
		rangeIcon = '/images/range-deactive.svg';
	}

	devices[iotDevice.device_id].range.setIcon({
		url: rangeIcon,
		scaledSize: new google.maps.Size(40, 40),
		anchor: new google.maps.Point(20, 20),
	});

	devices[iotDevice.device_id].marker.setPosition({
		lat: iotDevice.lat,
		lng: iotDevice.lng
	});

	devices[iotDevice.device_id].range.setPosition({
		lat: iotDevice.lat,
		lng: iotDevice.lng
	});

	const contentString = getInfoWindowContent(iotDevice, 'sensor');
	devices[iotDevice.device_id].iw.setContent(contentString);

}

function updateAndroidDeviceMarker(androidDevice) {

	const now = moment();
	const lastSeen = moment(androidDevice.lastUpdate * 1000);
	// If the device session timed-out
	if (now.diff(lastSeen, 'seconds') > 30) {
		devices[androidDevice.device_id].marker.setIcon({
			url: '/images/android-device-icon-inactive.svg',
			scaledSize: new google.maps.Size(40, 46),
			anchor: new google.maps.Point(20, 46),
		});
	} else {
		devices[androidDevice.device_id].marker.setIcon({
			url: '/images/android-device-icon.svg',
			scaledSize: new google.maps.Size(40, 46),
			anchor: new google.maps.Point(20, 46),
		});

	}

	devices[androidDevice.device_id].marker.setPosition({
		lat: androidDevice.lat,
		lng: androidDevice.lng
	});

	const contentString = getInfoWindowContent(androidDevice, 'android');
	devices[androidDevice.device_id].iw.setContent(contentString);

}

function toggleInfoWindow(device_id) {
	if (devices[device_id].iw.map) devices[device_id].iw.close();
	else devices[device_id].iw.open(map, devices[device_id].marker);
}

function getInfoWindowContent(device, deviceType) {

	if (deviceType === 'android') {
		const now = moment();
		const lastSeen = moment(device.lastUpdate * 1000);
		const content = `
			<div class="iw-content">
				<div><strong>Device ID: ${device.device_id}</strong></div>
				<div>Lat: ${device.lat}</div>
				<div>Lng: ${device.lng}</div>
				<div>Last seen: ${lastSeen.format('DD/MM/YYYY HH:mm:ss')}</div>
				<div>Seconds since last update: <br>${now.diff(lastSeen, 'seconds')}</div>
			</div>
		`
		return content;
	} else {
		const now = moment();
		const lastSeen = moment(device.lastUpdate * 1000);
		let dangerLevelText = "";
		if (device.dangerLevel === 'DANGER_LEVEL_HIGH') {
			dangerLevelText = `
				<div class='text-high-danger'>Υψηλός κίνδυνος</div>
				<div class='text-high-danger'>${device.eventMessage}</div>
				`;
		} else if (device.dangerLevel === 'DANGER_LEVEL_MEDIUM') {
			dangerLevelText = `
				<div class='text-medium-danger'>Μέτριος κίνδυνος</div>
				<div class='text-medium-danger'>${device.eventMessage}</div>
				`;
		}
		const content = `
			<div class="iw-content">
				<div><strong>Device ID: ${device.device_id}</strong></div>
				${dangerLevelText}
				<div>Lat: ${device.lat}</div>
				<div>Lng: ${device.lng}</div>
				<div>Battery level: ${device.battery_level}</div>
				<div>Last seen: ${lastSeen.format('DD/MM/YYYY HH:mm:ss')}</div>
				<div>Seconds since last update: <br>${now.diff(lastSeen, 'seconds')}</div>
			</div>
		`
		return content;
	}

}

function drawRectangle(sensors) {

	var bounds = new google.maps.LatLngBounds();
	const sensor1 = new google.maps.LatLng(sensors[0].lat, sensors[0].lng);
	const sensor2 = new google.maps.LatLng(sensors[1].lat, sensors[1].lng);
	bounds.extend(sensor1);
	bounds.extend(sensor2);
	if (rectangle) {
		rectangle.setBounds(bounds);
	} else {
		rectangle = new google.maps.Rectangle({
			map: map,
			bounds: bounds,
			strokeColor: "#FF0000",
			fillColor: "#FF0000",
			strokeOpacity: 0.0,
		});
	}

}

function clearRectangle() {
	if (rectangle) {
		rectangle.setMap(null);
		rectangle = null;
	}
}