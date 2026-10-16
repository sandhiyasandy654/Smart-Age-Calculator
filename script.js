
async function calculateAge() {
    const dob = document.getElementById("dob").value;

    if (!dob) {
        alert("Please select your date of birth!");
        return;
    }

    const birthDate = new Date(dob + "T00:00:00");
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    if (birthDate > today) {
        alert("Date of birth cannot be in the future!");
        return;
    }

    try {
        const response = await fetch(
            "/api/age?dob=" + encodeURIComponent(dob)
        );

        if (!response.ok) {
            throw new Error("Could not calculate age.");
        }

        const data = await response.json();

        document.getElementById("age").textContent =
            "Age: " + data.years + " years, " +
            data.months + " months, " +
            data.days + " days";

        document.getElementById("days").textContent =
            "Total Days: " + data.totalDays;

        document.getElementById("birthday").textContent =
            "Next Birthday: " + data.nextBirthday +
            " (" + data.daysUntilBirthday + " days left)";

        document.getElementById("category").textContent =
            "Age Category: " + data.category;

    } catch (error) {
        alert("Unable to calculate age. Please check the server.");
        console.error(error);
    }
}
