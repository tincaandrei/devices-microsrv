import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import "../styles/LandingPage.css";

export default function LandingPage() {
  const [credential, setCredential] = useState(null);
  const [userProfile, setUserProfile] = useState(null);
  const [devices, setDevices] = useState([]);
  const [availableDevices, setAvailableDevices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");

  const [newDeviceName, setNewDeviceName] = useState("");
  const [newDeviceMaxConsumption, setNewDeviceMaxConsumption] = useState("");
  const [newDevicePowerConsumption, setNewDevicePowerConsumption] =
    useState("");
  const [isSubmittingDevice, setIsSubmittingDevice] = useState(false);

  const [editingProfile, setEditingProfile] = useState(false);
  const [profileForm, setProfileForm] = useState({
    id: "",
    firstName: "",
    lastName: "",
    email: "",
    phoneNumber: "",
    address: "",
    city: "",
    country: "",
  });
  const [isSavingProfile, setIsSavingProfile] = useState(false);

  const navigate = useNavigate();

  const token = localStorage.getItem("authToken");

  useEffect(() => {
    if (!token) {
      navigate("/");
      return;
    }

    const commonHeaders = {
      Authorization: `Bearer ${token}`,
    };

    async function loadData() {
      try {
        setLoading(true);
        setErrorMessage("");

        const [authResponse, userDevicesResponse, availableDevicesResponse] =
          await Promise.all([
            fetch("http://localhost/auth/me", {
              headers: commonHeaders,
            }),
            fetch("http://localhost/users/me/devices", {
              headers: commonHeaders,
            }),
            fetch("http://localhost/devices/available", {
              headers: commonHeaders,
            }),
          ]);

        if (
          authResponse.status === 401 ||
          authResponse.status === 403 ||
          userDevicesResponse.status === 401 ||
          userDevicesResponse.status === 403 ||
          availableDevicesResponse.status === 401 ||
          availableDevicesResponse.status === 403
        ) {
          navigate("/");
          return;
        }

        if (!authResponse.ok) {
          throw new Error("Failed to load authentication data");
        }

        if (!userDevicesResponse.ok) {
          throw new Error("Failed to load user devices");
        }

        if (!availableDevicesResponse.ok) {
          throw new Error("Failed to load available devices");
        }

        const authData = await authResponse.json();
        const devicesData = await userDevicesResponse.json();
        const availableDevicesData = await availableDevicesResponse.json();

        setCredential(authData);
        setUserProfile(devicesData.user);
        setDevices(devicesData.devices || []);
        setAvailableDevices(availableDevicesData || []);

        setProfileForm({
          id: devicesData.user?.id ?? "",
          firstName: devicesData.user?.firstName ?? "",
          lastName: devicesData.user?.lastName ?? "",
          email: devicesData.user?.email ?? "",
          phoneNumber: devicesData.user?.phoneNumber ?? "",
          address: devicesData.user?.address ?? "",
          city: devicesData.user?.city ?? "",
          country: devicesData.user?.country ?? "",
        });
      } catch (error) {
        console.error("Error loading landing page data:", error);
        setErrorMessage("Could not load your data. Please try again.");
      } finally {
        setLoading(false);
      }
    }

    loadData();
  }, [token, navigate]);

  const isAdmin = credential?.role === "ADMIN";

  const handleAddDevice = async (event) => {
    event.preventDefault();
    if (
      !newDeviceName ||
      !newDeviceMaxConsumption ||
      !newDevicePowerConsumption
    ) {
      return;
    }

    if (!token) {
      return;
    }

    try {
      setIsSubmittingDevice(true);
      setErrorMessage("");

      const response = await fetch("http://localhost/devices", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          name: newDeviceName,
          description: "",
          maximumConsumption: parseFloat(newDeviceMaxConsumption),
          powerConsumption: parseFloat(newDevicePowerConsumption),
        }),
      });

      if (!response.ok) {
        const text = await response.text();
        throw new Error(text || "Failed to add device");
      }

      const createdDevice = await response.json();
      setAvailableDevices((previous) => [...previous, createdDevice]);
      setNewDeviceName("");
      setNewDeviceMaxConsumption("");
      setNewDevicePowerConsumption("");
    } catch (error) {
      console.error("Error adding device:", error);
      setErrorMessage("Could not add device. Please try again.");
    } finally {
      setIsSubmittingDevice(false);
    }
  };

  const handleAssignDeviceToMe = async (deviceId) => {
    if (!token || !userProfile?.id) {
      return;
    }

    try {
      setErrorMessage("");
      const response = await fetch(
        `http://localhost/devices/${deviceId}/assign/${userProfile.id}`,
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (!response.ok) {
        const text = await response.text();
        throw new Error(text || "Failed to assign device");
      }

      const updatedDevice = await response.json();
      setDevices((previous) => [...previous, updatedDevice]);
      setAvailableDevices((previous) =>
        previous.filter((device) => device.id !== deviceId)
      );
    } catch (error) {
      console.error("Error assigning device:", error);
      setErrorMessage("Could not assign device. Please try again.");
    }
  };

  const handleUnassignDevice = async (deviceId) => {
    if (!token) {
      return;
    }

    try {
      setErrorMessage("");
      const response = await fetch(
        `http://localhost/devices/${deviceId}/unassign`,
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (!response.ok) {
        const text = await response.text();
        throw new Error(text || "Failed to unassign device");
      }

      const updatedDevice = await response.json();
      setDevices((previous) =>
        previous.filter((device) => device.id !== deviceId)
      );
      setAvailableDevices((previous) => [...previous, updatedDevice]);
    } catch (error) {
      console.error("Error unassigning device:", error);
      setErrorMessage("Could not unassign device. Please try again.");
    }
  };

  const handleRemoveDevice = async (deviceId) => {
    if (!token) {
      return;
    }

    try {
      setErrorMessage("");
      const response = await fetch(`http://localhost/devices/${deviceId}`, {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok && response.status !== 204) {
        const text = await response.text();
        throw new Error(text || "Failed to remove device");
      }

      setDevices((previous) =>
        previous.filter((device) => device.id !== deviceId)
      );
    } catch (error) {
      console.error("Error removing device:", error);
      setErrorMessage("Could not remove device. Please try again.");
    }
  };

  const handleProfileFieldChange = (field, value) => {
    setProfileForm((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  const handleSaveProfile = async (event) => {
    event.preventDefault();
    if (!token || !profileForm.id) {
      return;
    }

    try {
      setIsSavingProfile(true);
      setErrorMessage("");

      const response = await fetch("http://localhost/users/me", {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(profileForm),
      });

      if (!response.ok) {
        const text = await response.text();
        throw new Error(text || "Failed to update profile");
      }

      const updatedProfile = await response.json();
      setUserProfile(updatedProfile);
      setEditingProfile(false);
    } catch (error) {
      console.error("Error updating profile:", error);
      setErrorMessage("Could not update profile. Please try again.");
    } finally {
      setIsSavingProfile(false);
    }
  };

  if (loading) {
    return (
      <div className="landing-page">
        <div className="landing-container">
          <p>Loading your data...</p>
        </div>
      </div>
    );
  }

  const displayName =
    userProfile && (userProfile.firstName || userProfile.lastName)
      ? `${userProfile.firstName ?? ""} ${userProfile.lastName ?? ""}`.trim()
      : credential?.username ?? "user";

  return (
    <div className="landing-page">
      <div className="landing-container">
        <header className="landing-header">
          <h1 className="landing-title">
            Hello, {displayName}
            {isAdmin && <span className="landing-role-badge">Admin</span>}
          </h1>
          <p className="landing-subtitle">
            Welcome to your energy management dashboard.
          </p>
        </header>

        {errorMessage && (
          <div className="landing-error-message">{errorMessage}</div>
        )}

        <main className="landing-main">
          <section className="landing-section landing-form-section">
            <h2 className="landing-section-title">My devices</h2>
            <p className="landing-section-description">
              Here you can see the devices linked to your account.
            </p>

            {devices.length === 0 ? (
              <p className="landing-devices-empty">
                You do not have any devices yet.
              </p>
            ) : (
              <ul className="landing-devices-list">
                {devices.map((device) => (
                  <li key={device.id} className="landing-device-item">
                    <div className="landing-device-main">
                      <span className="landing-device-name">{device.name}</span>
                      <span className="landing-device-consumption">
                        Max: {device.maximumConsumption} kWh • Power:{" "}
                        {device.powerConsumption} kW
                      </span>
                    </div>
                    {isAdmin ? (
                      <button
                        type="button"
                        className="landing-device-remove"
                        onClick={() => handleRemoveDevice(device.id)}
                      >
                        Remove
                      </button>
                    ) : (
                      <button
                        type="button"
                        className="landing-device-remove"
                        onClick={() => handleUnassignDevice(device.id)}
                      >
                        Unassign
                      </button>
                    )}
                  </li>
                ))}
              </ul>
            )}

            {isAdmin && (
              <form className="landing-form" onSubmit={handleAddDevice}>
                <h3 className="landing-subsection-title">Add device</h3>
                <div className="landing-form-field">
                  <label htmlFor="newDeviceName">Name</label>
                  <input
                    id="newDeviceName"
                    type="text"
                    value={newDeviceName}
                    onChange={(event) => setNewDeviceName(event.target.value)}
                    placeholder="e.g. Living room AC"
                    required
                  />
                </div>
                <div className="landing-form-field">
                  <label htmlFor="newDeviceMaxConsumption">
                    Maximum consumption (kWh)
                  </label>
                  <input
                    id="newDeviceMaxConsumption"
                    type="number"
                    min="0.1"
                    step="0.1"
                    value={newDeviceMaxConsumption}
                    onChange={(event) =>
                      setNewDeviceMaxConsumption(event.target.value)
                    }
                    required
                  />
                </div>
                <div className="landing-form-field">
                  <label htmlFor="newDevicePowerConsumption">
                    Power consumption (kW)
                  </label>
                  <input
                    id="newDevicePowerConsumption"
                    type="number"
                    min="0.0"
                    step="0.1"
                    value={newDevicePowerConsumption}
                    onChange={(event) =>
                      setNewDevicePowerConsumption(event.target.value)
                    }
                    required
                  />
                </div>
                <button
                  type="submit"
                  className="landing-form-submit"
                  disabled={isSubmittingDevice}
                >
                  {isSubmittingDevice ? "Adding..." : "Add device"}
                </button>
              </form>
            )}
          </section>

          <section className="landing-section landing-form-section">
            <h2 className="landing-section-title">Available devices</h2>
            <p className="landing-section-description">
              Devices that are not assigned to any user yet.
            </p>

            {availableDevices.length === 0 ? (
              <p className="landing-devices-empty">
                There are no available devices right now.
              </p>
            ) : (
              <ul className="landing-devices-list">
                {availableDevices.map((device) => (
                  <li key={device.id} className="landing-device-item">
                    <div className="landing-device-main">
                      <span className="landing-device-name">{device.name}</span>
                      <span className="landing-device-consumption">
                        Max: {device.maximumConsumption} kWh • Power:{" "}
                        {device.powerConsumption} kW
                      </span>
                    </div>
                    <button
                      type="button"
                      className="landing-device-remove"
                      onClick={() => handleAssignDeviceToMe(device.id)}
                    >
                      Assign to me
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </section>

          <section className="landing-section landing-user-section">
            <h2 className="landing-section-title">My profile</h2>
            <p className="landing-section-description">
              Basic information associated with your account.
            </p>

            {userProfile ? (
              <>
                {!editingProfile ? (
                  <>
                    <dl className="landing-user-details">
                      <div className="landing-user-row">
                        <dt>Name</dt>
                        <dd>
                          {userProfile.firstName} {userProfile.lastName}
                        </dd>
                      </div>
                      <div className="landing-user-row">
                        <dt>Email</dt>
                        <dd>{userProfile.email}</dd>
                      </div>
                      <div className="landing-user-row">
                        <dt>Phone</dt>
                        <dd>{userProfile.phoneNumber || "Not set"}</dd>
                      </div>
                      <div className="landing-user-row">
                        <dt>Address</dt>
                        <dd>{userProfile.address || "Not set"}</dd>
                      </div>
                      <div className="landing-user-row">
                        <dt>City</dt>
                        <dd>{userProfile.city || "Not set"}</dd>
                      </div>
                      <div className="landing-user-row">
                        <dt>Country</dt>
                        <dd>{userProfile.country || "Not set"}</dd>
                      </div>
                    </dl>
                    <button
                      type="button"
                      className="landing-form-submit"
                      onClick={() => setEditingProfile(true)}
                    >
                      Edit profile
                    </button>
                  </>
                ) : (
                  <form
                    className="landing-form landing-profile-form"
                    onSubmit={handleSaveProfile}
                  >
                    <div className="landing-form-field">
                      <label htmlFor="profileFirstName">First name</label>
                      <input
                        id="profileFirstName"
                        type="text"
                        value={profileForm.firstName}
                        onChange={(event) =>
                          handleProfileFieldChange(
                            "firstName",
                            event.target.value
                          )
                        }
                        required
                      />
                    </div>
                    <div className="landing-form-field">
                      <label htmlFor="profileLastName">Last name</label>
                      <input
                        id="profileLastName"
                        type="text"
                        value={profileForm.lastName}
                        onChange={(event) =>
                          handleProfileFieldChange(
                            "lastName",
                            event.target.value
                          )
                        }
                        required
                      />
                    </div>
                    <div className="landing-form-field">
                      <label htmlFor="profileEmail">Email</label>
                      <input
                        id="profileEmail"
                        type="email"
                        value={profileForm.email}
                        onChange={(event) =>
                          handleProfileFieldChange("email", event.target.value)
                        }
                        required
                      />
                    </div>
                    <div className="landing-form-field">
                      <label htmlFor="profilePhone">Phone</label>
                      <input
                        id="profilePhone"
                        type="text"
                        value={profileForm.phoneNumber}
                        onChange={(event) =>
                          handleProfileFieldChange(
                            "phoneNumber",
                            event.target.value
                          )
                        }
                      />
                    </div>
                    <div className="landing-form-field">
                      <label htmlFor="profileAddress">Address</label>
                      <input
                        id="profileAddress"
                        type="text"
                        value={profileForm.address}
                        onChange={(event) =>
                          handleProfileFieldChange(
                            "address",
                            event.target.value
                          )
                        }
                      />
                    </div>
                    <div className="landing-form-field">
                      <label htmlFor="profileCity">City</label>
                      <input
                        id="profileCity"
                        type="text"
                        value={profileForm.city}
                        onChange={(event) =>
                          handleProfileFieldChange("city", event.target.value)
                        }
                      />
                    </div>
                    <div className="landing-form-field">
                      <label htmlFor="profileCountry">Country</label>
                      <input
                        id="profileCountry"
                        type="text"
                        value={profileForm.country}
                        onChange={(event) =>
                          handleProfileFieldChange(
                            "country",
                            event.target.value
                          )
                        }
                      />
                    </div>
                    <div className="landing-profile-actions">
                      <button
                        type="button"
                        className="landing-device-remove"
                        onClick={() => {
                          setEditingProfile(false);
                          setProfileForm({
                            id: userProfile.id,
                            firstName: userProfile.firstName ?? "",
                            lastName: userProfile.lastName ?? "",
                            email: userProfile.email ?? "",
                            phoneNumber: userProfile.phoneNumber ?? "",
                            address: userProfile.address ?? "",
                            city: userProfile.city ?? "",
                            country: userProfile.country ?? "",
                          });
                        }}
                      >
                        Cancel
                      </button>
                      <button
                        type="submit"
                        className="landing-form-submit"
                        disabled={isSavingProfile}
                      >
                        {isSavingProfile ? "Saving..." : "Save profile"}
                      </button>
                    </div>
                  </form>
                )}
              </>
            ) : (
              <p className="landing-devices-empty">
                We could not load your profile details.
              </p>
            )}
          </section>
        </main>
      </div>
    </div>
  );
}

