document.querySelector('form').addEventListener('submit', event => {
    const dobValue = document.getElementById('dob').value;
    const match = dobValue.match(/^(\d{2})\/(\d{2})\/(\d{4})$/);

    if (!match) return stopSubmit(event, 'Please enter date in DD/MM/YYYY format');

    const day = Number(match[1]);
    const month = Number(match[2]) - 1;
    const year = Number(match[3]);
    const dob = new Date(year, month, day);
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    if (dob.getFullYear() !== year || dob.getMonth() !== month || dob.getDate() !== day) return stopSubmit(event, 'Invalid date entered!');
    if (dob > today) return stopSubmit(event, 'Date of Birth cannot be in the future!');
});

function stopSubmit(event, message) {
    alert(message);
    event.preventDefault();
}
