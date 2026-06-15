const dobInput = document.getElementById('dob');
const addressInput = document.getElementById('address');
const manualAddress = document.getElementById('manualAddress');
const locationStatus = document.getElementById('locationStatus');
const latitudeInput = document.getElementById('location_latitude');
const longitudeInput = document.getElementById('location_longitude');
const useLocationButton = document.getElementById('useLocationBtn');

const idNumberInput = document.getElementById('id_number');
const personalPhoneInput = document.getElementById('cell_number');
const emergencyPhoneInput = document.getElementById('emergency_contact_number');

const termsCheckbox = document.getElementById('termsCheckbox');
const submitBtn = document.getElementById('submitBtn');

if (dobInput) {
    setLatestAllowedDateOfBirth(dobInput);
}

/* ================= LOCATION ================= */

if (useLocationButton) {

    useLocationButton.addEventListener('click', () => {

        if (!navigator.geolocation) {

            showLocationMessage(
                'Location is not available on this device. Please enter your address manually.',
                true
            );

            if (manualAddress) {
                manualAddress.checked = true;
            }

            if (addressInput) {
                addressInput.focus();
            }

            return;
        }

        showLocationMessage('Requesting device location...', false);

        navigator.geolocation.getCurrentPosition(

            position => {

                const latitude = position.coords.latitude.toFixed(6);
                const longitude = position.coords.longitude.toFixed(6);

                if (latitudeInput) latitudeInput.value = latitude;
                if (longitudeInput) longitudeInput.value = longitude;

                if (addressInput) {
                    addressInput.value =
                        `Device location: ${latitude}, ${longitude}`;
                }

                if (manualAddress) {
                    manualAddress.checked = false;
                }

                showLocationMessage(
                    'Device location added successfully.',
                    false
                );
            },

            () => {

                showLocationMessage(
                    'Location permission denied. Please enter your address manually.',
                    true
                );

                if (manualAddress) {
                    manualAddress.checked = true;
                }

                if (addressInput) {
                    addressInput.focus();
                }
            },

            {
                enableHighAccuracy: true,
                timeout: 10000,
                maximumAge: 60000
            }
        );
    });
}

/* ================= TERMS CHECKBOX ================= */

if (termsCheckbox && submitBtn) {

    function toggleSubmitButton() {

        if (termsCheckbox.checked) {

            submitBtn.disabled = false;

        } else {

            submitBtn.disabled = true;
        }
    }

    termsCheckbox.addEventListener('change', toggleSubmitButton);

    toggleSubmitButton();
}

/* ================= FORM VALIDATION ================= */

document.querySelector('form').addEventListener('submit', event => {

    /* TERMS CHECK */
    if (termsCheckbox && !termsCheckbox.checked) {

        return stopSubmit(
            event,
            'You must accept the Terms & Conditions before continuing.'
        );
    }

    /* ID NUMBER */
    const idNumber = (idNumberInput?.value || '').trim();

    if (idNumberInput && !/^[0-9]{13}$/.test(idNumber)) {

        return stopSubmit(
            event,
            'Please enter a valid 13 digit South African ID number.'
        );
    }

    if (idNumberInput && idNumber && !dateFromSouthAfricanId(idNumber)) {

        return stopSubmit(
            event,
            'South African ID number must contain a real past birth date.'
        );
    }

    /* PHONE NUMBERS */
    const personalPhone = normalizePhone(personalPhoneInput?.value || '');
    const emergencyPhone = normalizePhone(emergencyPhoneInput?.value || '');

    if (personalPhoneInput && !isValidPhone(personalPhone)) {

        return stopSubmit(
            event,
            'Please enter a valid South African personal cell number.'
        );
    }

    if (emergencyPhoneInput && !isValidPhone(emergencyPhone)) {

        return stopSubmit(
            event,
            'Please enter a valid South African emergency contact number.'
        );
    }

    if (
        personalPhoneInput &&
        emergencyPhoneInput &&
        personalPhone === emergencyPhone
    ) {

        return stopSubmit(
            event,
            'Your personal number and emergency contact number must not be the same.'
        );
    }

    /* DATE OF BIRTH */
    const dobValue = dobInput?.value;

    if (dobInput && !dobValue) {

        return stopSubmit(
            event,
            'Please select your date of birth.'
        );
    }

    if (dobInput) {

        const dob = new Date(`${dobValue}T00:00:00`);

        const today = new Date();
        today.setHours(0, 0, 0, 0);

        if (Number.isNaN(dob.getTime())) {

            return stopSubmit(
                event,
                'Invalid date selected.'
            );
        }

        if (dob >= today) {

            return stopSubmit(
                event,
                'Date of Birth must be before today.'
            );
        }

        const idDate = idNumber ? dateFromSouthAfricanId(idNumber) : null;
        if (idNumberInput && idDate && idDate.getTime() !== dob.getTime()) {

            return stopSubmit(
                event,
                'South African ID number must match the selected date of birth.'
            );
        }
    }

    /* ADDRESS */
    if (addressInput && !addressInput.value.trim()) {

        return stopSubmit(
            event,
            'Please add your device location or enter your address manually.'
        );
    }
});

/* ================= HELPERS ================= */

function stopSubmit(event, message) {

    alert(message);
    event.preventDefault();
}

function showLocationMessage(message, isError) {

    if (!locationStatus) return;

    locationStatus.textContent = message;

    locationStatus.className = isError
        ? 'form-note error'
        : 'form-note success';
}

function normalizePhone(value) {

    const digits = value.replace(/\D/g, '');

    if (digits.startsWith('27') && digits.length === 11) {

        return `0${digits.slice(2)}`;
    }

    return digits;
}

function isValidPhone(value) {

    return /^0[6-8][0-9]{8}$/.test(value);
}

function dateFromSouthAfricanId(idNumber) {

    if (!/^[0-9]{13}$/.test(idNumber)) {
        return null;
    }

    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const currentCentury = Math.floor(today.getFullYear() / 100) * 100;
    const year = Number(idNumber.slice(0, 2));
    const month = Number(idNumber.slice(2, 4));
    const day = Number(idNumber.slice(4, 6));
    let date = new Date(currentCentury + year, month - 1, day);

    if (date >= today) {
        date = new Date(currentCentury - 100 + year, month - 1, day);
    }

    if (
        date.getFullYear() % 100 !== year ||
        date.getMonth() !== month - 1 ||
        date.getDate() !== day ||
        date >= today
    ) {
        return null;
    }

    return date;
}
