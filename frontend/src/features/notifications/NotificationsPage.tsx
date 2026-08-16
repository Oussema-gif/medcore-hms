import { useState } from "react";
import {
  Bell,
  Check,
  ChevronLeft,
  ChevronRight,
  RefreshCw,
} from "lucide-react";
import {
  useMutation,
  useQuery,
  useQueryClient,
} from "@tanstack/react-query";
import {
  getNotifications,
  markNotificationAsRead,
} from "./api";

const PAGE_SIZE = 10;

export function NotificationsPage() {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);

  const notificationsQuery = useQuery({
    queryKey: ["notifications", page, PAGE_SIZE],
    queryFn: () => getNotifications(page, PAGE_SIZE),
  });

  const markReadMutation = useMutation({
    mutationFn: markNotificationAsRead,
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["notifications"],
      });
    },
  });

  const response = notificationsQuery.data;
  const notifications = response?.data ?? [];
  const hasPreviousPage = page > 0;
  const hasNextPage = response
    ? page + 1 < response.totalPages
    : false;

  return (
    <section>
      <div className="page-header">
        <div>
          <h2>Notifications</h2>
          <p>Stay up to date with your hospital activity.</p>
        </div>

        <button
          className="app-button"
          type="button"
          onClick={() => notificationsQuery.refetch()}
          disabled={notificationsQuery.isFetching}
        >
          <RefreshCw size={16} />
          Refresh
        </button>
      </div>

      {notificationsQuery.isLoading && (
        <div className="card empty-state">
          Loading notifications...
        </div>
      )}

      {notificationsQuery.isError && (
        <div className="alert" role="alert">
          Unable to load notifications. Please try again.
        </div>
      )}

      {!notificationsQuery.isLoading &&
        !notificationsQuery.isError &&
        notifications.length === 0 && (
          <div className="card empty-state">
            <Bell size={32} />
            <p>You have no notifications.</p>
          </div>
        )}

      {notifications.length > 0 && (
        <div className="card-grid">
          {notifications.map((notification) => (
            <article className="card" key={notification.id}>
              <div className="page-header">
                <div>
                  <h3>{notification.title}</h3>
                  <p>{notification.body}</p>
                </div>

                {!notification.readAt && (
                  <span className="app-user-pill">Unread</span>
                )}
              </div>

              <p className="helper-text">
                {new Date(notification.createdAt).toLocaleString()}
              </p>

              {!notification.readAt && (
                <button
                  className="app-button"
                  type="button"
                  onClick={() =>
                    markReadMutation.mutate(notification.id)
                  }
                  disabled={markReadMutation.isPending}
                >
                  <Check size={16} />
                  Mark as read
                </button>
              )}
            </article>
          ))}
        </div>
      )}

      {response && response.totalPages > 0 && (
        <div className="pagination-bar">
          <span className="helper-text">
            Page {response.page + 1} of {response.totalPages}
            {" · "}
            {response.totalElements} total
          </span>

          <div className="pagination-controls">
            <button
              className="app-button"
              type="button"
              onClick={() => setPage((current) => current - 1)}
              disabled={!hasPreviousPage || notificationsQuery.isFetching}
              aria-label="Previous page"
            >
              <ChevronLeft size={16} />
              Previous
            </button>

            <button
              className="app-button"
              type="button"
              onClick={() => setPage((current) => current + 1)}
              disabled={!hasNextPage || notificationsQuery.isFetching}
              aria-label="Next page"
            >
              Next
              <ChevronRight size={16} />
            </button>
          </div>
        </div>
      )}
    </section>
  );
}
