var map;
var devices = {};
var rectangle = null;
var timeOut = 30;
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

	axios.get("http://localhost:8080/api/devices/")
		.then(function (response) {
			populateMarkers(response.data);
		})
		.catch(function (error) {
			console.error(error.message);
			console.error('Something went wrong!\nTrying again in 10 seconds...');
			setTimeout(getDevices, 10 * 1000);
		})

}

function populateMarkers(devicesData) {

	devicesData.androidDevicesList.forEach(function (androidDevice) { addAndroidDeviceMarker(androidDevice); });
	devicesData.iotDevicesList.forEach(function (iotDevice) { addIotDeviceMarker(iotDevice) });

	// Check if there 2 active events from 2 different sensors (aka in the last $timeOut seconds)
	clearRectangle();
	if (devicesData.iotDevicesList.length >= 2) {
		let event1 = false, event2 = false;
		for (s1 = 0; s1 < devicesData.iotDevicesList.length; s1++)
			if (moment().diff(devicesData.iotDevicesList[s1].lastUpdate * 1000, 'seconds') <= timeOut &&
				devicesData.iotDevicesList[s1].dangerLevel !== 'NONE') { event1 = true; break; }
		for (s2 = 0; s2 < devicesData.iotDevicesList.length; s2++)
			if (moment().diff(devicesData.iotDevicesList[s2].lastUpdate * 1000, 'seconds') <= timeOut &&
				s1 != s2 && devicesData.iotDevicesList[s2].dangerLevel !== 'NONE') { event2 = true; break; }
		if (event1 && event2) drawRectangle(devicesData.iotDevicesList[s1], devicesData.iotDevicesList[s2]);
	}

	setTimeout(() => { getDevices(); }, 1 * 1000);

}

function addAndroidDeviceMarker(androidDevice) {

	// If device doesn't exist, create its context
	if (!devices[androidDevice.device_id]) {
		const marker = new google.maps.Marker({map: map,});
		const infoWindow = new google.maps.InfoWindow({anchor: marker});

		devices[androidDevice.device_id] = {
			'device': androidDevice,
			'deviceType': 'android-device',
			'marker': marker,
			'iw': infoWindow,
		};

		marker.addListener("click", () => { toggleInfoWindow(androidDevice.device_id); });
		updateAndroidDeviceMarker(androidDevice);
    }

    // Update device info on the map
    updateAndroidDeviceMarker(androidDevice);
}

function addIotDeviceMarker(device) {

	// If device doesn't exist, create crete its context
    if (!devices[device.device_id]) {
		const marker = new google.maps.Marker({map: map});
		const infoWindow = new google.maps.InfoWindow({anchor: marker});
		const rangeMarker = new google.maps.Marker({
			optimized: false,
			map: map
		});

		devices[device.device_id] = {
			'device': device,
			'deviceType': 'sensor-device',
			'marker': marker,
			'iw': infoWindow,
			'range': rangeMarker,
		};

		rangeMarker.addListener("click", () => { toggleInfoWindow(device.device_id); });
		marker.addListener("click", () => { toggleInfoWindow(device.device_id); });
		updateIotDeviceMarker(device);
    }

    // Update device info on the map
    updateIotDeviceMarker(iotDevice);

}

function updateAndroidDeviceMarker(device) {

	const lastSeen = moment(device.lastUpdate * 1000);
    let iconUrl = './images/android-device-online-icon.svg';
	// Check if the device session timed-out and set the appropriate status icon
    if (moment().diff(lastSeen, 'seconds') > timeOut) iconUrl = './images/android-device-offline-icon.svg';

	devices[device.device_id].marker.setIcon({
		url: iconUrl,
		scaledSize: new google.maps.Size(40, 46),
		anchor: new google.maps.Point(20, 46),
	});

	devices[device.device_id].marker.setPosition({
		lat: device.lat,
		lng: device.lng
	});

	devices[device.device_id].iw.setContent(getInfoWindowContent(device, 'android'));

}

function updateIotDeviceMarker(iotDevice) {

	const lastSeen = moment(iotDevice.lastUpdate * 1000);
	let iconUrl = '/images/iot-device-online-icon.svg', rangeIcon = './images/range-online.svg';
	let emptyPayload = iotDevice.smoke == null && iotDevice.gas == null && iotDevice.temperature == null && iotDevice.uv == null;
	// Check if the device has triggered any danger event
	if (iotDevice.dangerLevel == 'DANGER_LEVEL_MEDIUM') iconUrl = './images/iot-medium-danger-icon.svg';
	if (iotDevice.dangerLevel == 'DANGER_LEVEL_HIGH') iconUrl = './images/iot-high-danger-icon.svg';
	// Check if IoT device didn't send sensor data or its session timed out
	if (emptyPayload || moment().diff(lastSeen, 'seconds') > timeOut) rangeIcon = './images/range-offline.svg';

	devices[iotDevice.device_id].marker.setIcon({
		url: iconUrl,
		scaledSize: new google.maps.Size(40, 46),
		anchor: new google.maps.Point(20, 46),
	});

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

	devices[iotDevice.device_id].iw.setContent(getInfoWindowContent(iotDevice, 'sensor'));

}

function toggleInfoWindow(device_id) {
	if (devices[device_id].iw.map) devices[device_id].iw.close();
	else devices[device_id].iw.open(map, devices[device_id].marker);
}

function getInfoWindowContent(device, deviceType) {

	const lastSeen = moment(device.lastUpdate * 1000);
	let sensorData = "";
	if (deviceType === 'sensor') {
		let dangerMessage = "";
		if (device.dangerLevel === 'DANGER_LEVEL_HIGH') {
			dangerMessage = `
				<div class='text-high-danger'>High Danger</div>
				<div class='text-high-danger'>${device.eventMessage}</div>
				`;
		} else if (device.dangerLevel === 'DANGER_LEVEL_MEDIUM') {
			dangerMessage = `
				<div class='text-medium-danger'>Medium Danger</div>
				<div class='text-medium-danger'>${device.eventMessage}</div>
				`;
		}
		sensorData = `
			<div>Battery level: ${device.battery_level}</div>
			${dangerMessage}
		`
	}

	return `
		<div class="iw-content">
			<div><strong>Device ID: ${device.device_id}</strong></div>
			<div>Lat: ${device.lat}</div>
			<div>Lng: ${device.lng}</div>
			${sensorData}
			<div>Last seen: ${lastSeen.format('DD/MM/YYYY HH:mm:ss')}</div>
			<div>Seconds since last update: ${moment().diff(lastSeen, 'seconds')}</div>
		</div>
	`

}

function drawRectangle(sensor1, sensor2) {

	var bounds = new google.maps.LatLngBounds();
	const point1 = new google.maps.LatLng(sensor1.lat, sensor1.lng);
	const point2 = new google.maps.LatLng(sensor2.lat, sensor2.lng);
	bounds.extend(point1);
	bounds.extend(point2);
	if (rectangle) rectangle.setBounds(bounds);
	else {
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