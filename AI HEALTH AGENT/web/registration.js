const dobInput = document.getElementById('dob');
const addressInput = document.getElementById('address');
const manualAddress = document.getElementById('manualAddress');
const locationStatus = document.getElementById('locationStatus');
const latitudeInput = document.getElementById('location_latitude');
const longitudeInput = document.getElementById('location_longitude');
const useLocationButton = document.getElementById('useLocationBtn');

if (dobInput) {
    dobInput.max = new Date().toISOString().split('T')[0];
}

if (useLocationButton) {
    useLocationButton.addEventListener('click', () => {
        if (!navigator.geolocation) {
            showLocationMessage('Location is not available on this device. Please enter your address manually.', true);
            manualAddress.checked = true;
            addressInput.focus();
            return;
        }

        showLocationMessage('Requesting device location...', false);
        navigator.geolocation.getCurrentPosition(
            position => {
                const latitude = position.coords.latitude.toFixed(6);
                const longitude = position.coords.longitude.toFixed(6);
                latitudeInput.value = latitude;
                longitudeInput.value = longitude;
                addressInput.value = `Device location: ${latitude}, ${longitude}`;
                manualAddress.checked = false;
                showLocationMessage('Device location added. You can still edit the address field if needed.', false);
            },
            () => {
                showLocationMessage('Location permission was not granted. Please enter your address manually.', true);
                manualAddress.checked = true;
                addressInput.focus();
            },
            { enableHighAccuracy: true, timeout: 10000, maximumAge: 60000 }
        );
    });
}

document.querySelector('form').addEventListener('submit', event => {
    const dobValue = dobInput.value;
    if (!dobValue) return stopSubmit(event, 'Please select your date of birth.');

    const dob = new Date(`${dobValue}T00:00:00`);
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    if (Number.isNaN(dob.getTime())) return stopSubmit(event, 'Invalid date selected.');
    if (dob > today) return stopSubmit(event, 'Date of Birth cannot be in the future!');

    if (!addressInput.value.trim()) {
        return stopSubmit(event, 'Please add your device location or enter your address manually.');
    }
});

function stopSubmit(event, message) {
    alert(message);
    event.preventDefault();
}

function showLocationMessage(message, isError) {
    locationStatus.textContent = message;
    locationStatus.className = isError ? 'form-note error' : 'form-note success';
}
